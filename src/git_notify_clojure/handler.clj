(ns git-notify-clojure.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [tentacles.users :as users]
            [monger.core :as mg]
            [monger.collection :as mc]))

(def conn (mg/connect))
(def db (mg/get-db conn "git-notifier-clojure"))

(defn get-users []
    (mc/find-maps db "users"))

(defn repo-users []
  (map :first_name (get-users)))
  ;(str (:avatar_url (users/user "sdorunga1"))))

(defn create-user
  [user]
  (mc/insert db "users" { :first_name "John" :last_name "Lennon" }))

(defroutes app-routes
  (GET "/" [] (repo-users))
  (GET "/create_user" [] 
       (do
         (create-user "Joe")
         "Success"))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

