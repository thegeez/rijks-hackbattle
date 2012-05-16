(ns hackbattle.sql
  (:require [clojure.java.jdbc :as sql]))

(def db (or (System/getenv "SHARED_DATABASE_URL")
            "postgres://hb:hb@localhost:5432/hb"))


(defn create-markers-table []
  (sql/with-connection db
    (sql/create-table :markers
                      [:id :serial "PRIMARY KEY"]
                      [:work_id :varchar "NOT NULL"]
                      [:px :integer "NOT NULL"]
                      [:py :integer "NOT NULL"]
                      [:content :varchar "NOT NULL"])))

(defn drop-tables []
  (sql/with-connection db
    (sql/drop-table :markers)))

(defn -main [& [cmd]]
  (println "Setting up database")
  (try
    (when (= cmd "drop")
      (drop-tables))
    (create-markers-table)
    (catch Exception e
      (sql/print-sql-exception e)))
  (println "Done!"))

(defn get-markers [work_id]
  (sql/with-connection db
    (sql/with-query-results res ["SELECT * FROM markers WHERE work_id=?" work_id]
      (doall res))))

(defn insert-marker [{:keys [id work_id px py content] :as marker :or {:id 0}}]
  (let [res (sql/with-connection db
              (sql/update-or-insert-values :markers
                                           ["id=?" id]
                                           {:work_id work_id
                                            :px px
                                            :py py
                                            :content content}))]
    (if (:id res)
      res
      marker)))

(defn delete [& [work-id marker-id]]
  (sql/with-connection db
    (sql/delete-rows :markers ["id=? AND work_id=?" (Integer/parseInt marker-id) work-id])))

(comment
  (insert-marker {:id 8
                  :work_id "SK-C-5"
                  :px 2
                  :py 2
                  :content "AAAFIRST DB marker"})
  
  (sql/with-connection db
    (sql/update-or-insert-values :markers
                                 ["id=?" 0]
                                 {:px 10
                                  :py 10
                                  :content "hahahahahah"}))
  )

(defn marker-count [work_id]
  (sql/with-connection db
    (sql/with-query-results res ["SELECT count(*) FROM markers WHERE work_id = ?" work_id]
      (:count (first res)))))
