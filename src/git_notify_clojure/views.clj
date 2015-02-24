(ns git-notify-clojure.views
  (:use [hiccup core page]))

(defn users-page [repo-users]
  (html5
    [:head
     [:title "Contributors"] ]
    [:body 
     [:h1 "Contributors"]
     [:table
      [:tr 
       [:th "Slack username"]
       [:th "Notify"]]
      (for [user repo-users]
        (list
          [:tr 
           [:td (:slack-username user)]
           [:td (:notify user)]] 
          ))]]))
