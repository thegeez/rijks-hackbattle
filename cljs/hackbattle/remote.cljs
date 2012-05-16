;; from clojurescriptone
(ns hackbattle.remote
  (:require [goog.net.XhrManager   :as manager]))
	
(def ^:private
  *xhr-manager*
  (goog.net.XhrManager. nil
                        nil
                        nil
                        0
                        5000))
	
(defn success?
  [{status :status}]
  (and (>= status 200)
       (<  status 300)))
	
(defn redirect?
  [{status :status}]
  (boolean (#{301 302 303 307} status)))
	
(defn error?
  [{status :status}]
  (>= status 400))
	
(defn- handle-response
  [on-success on-error e]
  (let [response {:id     (.-id e)
                  :body   (. e/currentTarget (getResponseText))
                  :status (. e/currentTarget (getStatus))
                  :event  e}
        handler  (if (success? response)
                   on-success
                   on-error)]
    (handler response)))

(defn request
  [id url & {:keys [method content headers priority retries
                    on-success on-error]
             :or   {method   "GET"
                    retries  0}}]
  (try
    (.send *xhr-manager*
           id
           url
           method
           content
           (when headers (.-strobj headers))
           priority
           (partial handle-response on-success on-error)
           retries)
    (catch js/Error e
      nil)))
	
(defn url
  [path]
  (str (.-origin (.-location js/document)) path))
