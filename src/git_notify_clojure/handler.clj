(ns git-notify-clojure.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body]]
            [tentacles.users :as users]
            [tentacles.repos :as repos]
            [monger.core :as mg]
            [monger.collection :as mc]))

(def conn (mg/connect))
(def db (mg/get-db conn "git-notifier-clojure"))

(defn get-users []
    (mc/find-maps db "users"))

(defn repo-users []
  (get-users))

(defn create-user
  [user]
  (println user)
  (mc/upsert db "users" {:github-username user} { :slack-username user :notify false }))

(defn contributors  [user-id repo-name]
    (repos/contributors user-id repo-name))

(defn top-contributors [user-name repo-name]
  (let [contributors (contributors user-name repo-name)]
    (->> contributors
         (sort-by :contributions >)
         (take 3)
         (shuffle)
         (take 1))))

(defn parse-users [string]
  (map last (re-seq #"@([\w+-]+)" string)))

(defn handle_webhook [request] 
  (let [{body :body} request
        {pr :pull_request repository :repository} body
        {pr_body :body} pr
        top-contributors (top-contributors (:login (:user pr)) (:name repository))
        mentioned-users (parse-users pr_body)
        notifiable-users (into (map :login top-contributors) mentioned-users)]
     (create-user (first notifiable-users))))

(defroutes app-routes
  (GET "/" []  (do (println "hi") (repo-users)))
  (GET "/create_user" [] 
       (do
         (create-user "Joe")
         "Success"))
  (POST "/webhooks" request (do (handle_webhook request) "Yay"))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-json-body {:keywords? true})))

