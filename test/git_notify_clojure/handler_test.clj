(ns git-notify-clojure.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [clojure.data.json :as json]
            [git-notify-clojure.handler :refer :all]))

(deftest contributor-tests
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))
      ;(is (= (:body response) "Hello World"))
      ))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

(deftest webhook-tests
  (testing "main route"
    (let [payload (json/write-str {:body 
                                    {:pull_request 
                                     {:body "Hello @sdorunga"
                                      :user {:login "sdorunga1"}}
                                     :repository {:name "bogus-library"}
                                     
                                     }})
          request (->
                   (mock/request :post "/webhooks")
                   (mock/content-type "application/json")
                   (mock/body payload))
          response (app request)]
      (is (= (:status response) 200)))))

