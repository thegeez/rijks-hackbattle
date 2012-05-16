(ns hackbattle.markers
  (:require [net.thegeez.browserchannel :as browserchannel]
            [rm.core :as rm]
            [hackbattle.sql :as sql]))

(def markers [{:id "lol"
               :px 10
               :py 10
               :content "This is the first marker"}])

(def session-work (atom {}))

(defn session-handler [session-id]
  (browserchannel/add-listener
   session-id
   :map
   (fn [map]
     ;; @todo pas clojure around
     (println "GOT MAP: " session-id "-" map)
     (cond
      (= "watching" (get map "type"))
      (when-let [work_id (get map "work_id")]
        (println "session " session-id " sent " map " is watching " work_id)
        (when (some #(= (:id %) work_id) rm/works)
          (swap! session-work assoc session-id work_id))
        (doseq [marker (sql/get-markers work_id)]
          (browserchannel/send-map session-id {"marker" (str marker)})))
      (= "marker" (get map "type"))
      (when-let [work_id (get map "work_id")]
        (let [id (get map "id")
              ;; yikes..
              is-new (or (nil? id)
                         (= "null" id)
                         (= "new_marker" id)
                         (= "" id))
              _ (println "is-new" is-new)
              id (Integer/parseInt (if is-new  "0" id)) 
              px (Integer/parseInt (get map "px"))
              py (Integer/parseInt (get map "py"))
              content (get map "content")]
            (when (and id
                    (some #(= (:id %) work_id) rm/works)
                    px
                    py
                    content)
              (let [marker (try (sql/insert-marker {:id id :work_id work_id :px px :py py :content content})
                                (catch Exception e
                                  (.printStackTrace e)
                                  (println (.getNextException e))))]
                (println "INSERTED marker" marker)
                (doseq [s-id (for [[k v] @session-work
                                      :when (= v work_id)]
                               k)]
                  (browserchannel/send-map s-id {"marker"
                                                 (str marker)}))))))))))
