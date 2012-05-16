(ns hackbattle.core
  (:use [net.cgrand.moustache :only (app)])
  (:require [net.thegeez.browserchannel :as browserchannel]
            [net.thegeez.jetty-async-adapter :as jetty]
            (ring.middleware [resource :as resource]
                             [file :as file]
                             [file-info :as file-info]
                             [params :as params])
            [hackbattle.pages :as pages]
            [hackbattle.markers :as markers]))

(def handler
  (app
   ["work"] pages/work
   [&] pages/index
   ))

;; (defn handler [req]
;;   (if (and (.startsWith (:uri req) "/_add_item")
;;            (= :post (:request-method req)))
;;     (let [body (read-string (get-in req [:form-params "data"] "nil"))]
;;       (println "BODY" body)
;;       (if (= (:desc body) "")
;;         {:status 300
;;          :headers {"Content-Type" "text/plain"}
;;          :body (pr-str "DESCRIPTION CAN'T BE EMPTY!")}
;;         {:status 200
;;          :headers {"Content-Type" "text/plain"}
;;          :body (pr-str (assoc body :id (let [id (:id body)]
;;                                          (if (= id "add-item")
;;                                            (str (gensym "item-id"))
;;                                            id))))}))
;;     {:status 200
;;      :headers {"Content-Type" "text/plain"}
;;      :body "Hello World!?"}))

(def dev-app
  (-> #'handler
      ((fn [handler]
         (fn [req]
           (require 'hackbattle.pages :reload)
           (handler req))))
      #_(file/wrap-file "/home/mfex/Programming/google-closure-library-r1732/closure/goog")
      (resource/wrap-resource "dev")
      (resource/wrap-resource "public")
      file-info/wrap-file-info
      params/wrap-params
      (browserchannel/wrap-browserchannel {:base "/channel"
                                           :on-session markers/session-handler})
      #_((fn [handler]
         (let [conn swank.core.connection/*current-connection*]
           (fn [request]
             (swank.core.connection/with-connection conn
               (handler request))))))
      ))

(defn -main [& args]
  (println "Using Jetty async adapter")
  (jetty/run-jetty-async #'dev-app {:port (Integer.
                                     (or
                                      (System/getenv "PORT")
                                      8000)) :join? false}))

(comment
  (do
    (.stop jetty-server)
    (def jetty-server (-main))
    )
  )
