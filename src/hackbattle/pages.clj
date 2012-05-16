(ns hackbattle.pages
  (:require [net.cgrand.enlive-html :as html]
            [ring.util.response :as response]
            [rm.core :as rm]
            [hackbattle.sql :as sql]))

(defn cut-off [s n]
  (let [l (count s)]
    (if (<= l n)
      s
      (str (subs s 0 n) "..."))))

(defn render [tags]
  (apply str tags))

(html/deftemplate index-page "pages/index.html"
  [req]
  [:span.work] (html/clone-for [{:keys [id title creator]} rm/works]
                               [:a] (html/set-attr :href (str "work?work=" id "&size=medium"))
                               [:span#thumb :img] (html/set-attr :src (str "http://www.rijksmuseum.nl/media/assets/" id "?100x100"))
                               [:span#title] (html/content (cut-off title 25))
                               [:span#creator] (html/content (cut-off creator 25))
                               [:span#notes] (html/content (str "notes: " (sql/marker-count id)))))

(defn index [req]
  (-> req
      index-page
      render
      response/response))

(html/deftemplate work-page "dev/index-dev.html"
  [{:keys [id title creator width height] :as work}]
  [:div#work] (html/set-attr "data_work_id" id
                             "data_width" width
                             "data_height" height)
  [:div#work :img] (html/set-attr :work_id id
                                  #_(str "http://www.rijksmuseum.nl/media/assets/" id #_"?aria/maxwidth_288"))
  [:div#title] (html/content title)
  [:div#creator] (html/content creator))

(defn work [req]
  (let [work-id (get (:query-params req) "work")]
    (if-let [work (some #(when (= (:id %) work-id) %) rm/works)]
      (response/response (render (work-page work)))
      (response/not-found "NOT FOUND"))))
