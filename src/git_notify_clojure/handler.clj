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
  (map :first_name (get-users)))

(defn create-user
  [user]
  (mc/insert db "users" { :first_name "John" :last_name "Lennon" }))


(defn repo-map  [{id :id name :name}]  {:id id :name name})

(defn contributors  [user-id repo-name]
    (repos/contributors user-id repo-name))

(defn top-contributors [user-name repo-name]
  (let [contributors (contributors user-name repo-name)]
    (->> contributors
         (sort-by :contributions >)
         (take 3)
         (shuffle)
         (take 1))))

(defn handle_webhook [request] 
  (let [{body :body} request
        {pr :pull_request} body
        {pr_body :body} pr
        ]
     (println pr_body)))

(defroutes app-routes
  (GET "/" []  (do (println "hi") (repo-users)))
  (GET "/create_user" [] 
       (do
         (create-user "Joe")
         "Success"))
  (POST "/webhooks" request (do (println request) (handle_webhook request) "Yay"))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-json-body {:keywords? true})))

