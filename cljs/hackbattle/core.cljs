(ns hackbattle.core
  (:require [clojure.browser.repl :as repl]
            [cljs.reader :as reader]
            [hackbattle.dom-helpers :as dom]
            [hackbattle.remote :as remote]
            [goog.events :as events]
            [goog.events.KeyCodes :as key-codes]
            [goog.events.KeyHandler :as key-handler]
            [goog.ui.Popup :as popup]
            [goog.ui.ScrollFloater :as scroll-floater]
            [goog.net.BrowserChannel :as goog-browserchannel]
            [goog.positioning :as avp]))

(defn work_id []
  (first (:work (dom/query-params))))

(defn work_size []
  (or (first (:size (dom/query-params)))
      "medium"))

(defn work_img_size []
  (let [size (first (:size (dom/query-params)))]
    (condp = size
     "medium" "?aria/maxwidth_288"
     "tiny" "?100x100"
     "small" "?200x200"
     "large" ""
     ""
     )))

(defn marker-side []
  (let [size (first (:size (dom/query-params)))]
    (condp = size
     "medium" 44
     "small" 22
     "large" 100 
     100
     )))

(defn place-note [marker text]
  (let [edit-el (dom/build [:span.edit "edit"])
        close-el (dom/build [:span.close "close"])
        view-text-el (dom/build [:div.content text])
        view-el (dom/build [:div.view
                            edit-el
                            close-el
                            view-text-el
                            ])
        save-el (dom/build [:span.save "save"])
        discard-el (dom/build [:span.discard "discard"])
        editor-text-el (dom/build [:textarea.content ""])
        editor-el (dom/build [:div.editor
                              save-el
                              discard-el
                              editor-text-el])
        popup-el (dom/build [:div.notes_editor {:style "position:absolute;
width: 250px;"}
                             view-el])
        popup (doto (goog.ui.Popup. popup-el)
                (.setHideOnEscape true))]
    (events/listen edit-el
                       "click"
                       (fn [e]
                         (.setEditable marker true)
                         (.preventDefault e ())))
    (events/listen close-el
                       "click"
                       (fn [e]
                         ;; @todo setVisible popup should be sufficient
                         (dom/show-element popup-el false)
                         (.preventDefault e ())))
    (events/listen save-el
                       "click"
                       (fn [e]
                         (.setNote marker (dom/get-value editor-text-el))
                         (.setEditable marker false)
                         (.preventDefault e ())))
    (events/listen discard-el
                       "click"
                       (fn [e]
                         ;; @todo setVisible popup should be sufficient
                         (dom/show-element popup-el false)
                         (.preventDefault e ())))
    ;;(dom/show-element popup-el false)
    ;;(dom/append item popup-el)
    (dom/append marker popup-el)
    (doto popup
      (.setVisible false)
      (.setPosition (goog.positioning.AnchoredViewportPosition. marker 5
                                                                true
                                                                ))
      ;;(.setVisible true)
      )
    (dom/show-element popup-el false)
    (events/listen marker "click" (fn [e]
                                    ;; firefox hack for text selection
                                    (js* "if (goog.userAgent.GECKO &&  window.getSelection) {window.getSelection().removeAllRanges();};")
                                    (.setVisible popup true)
                                    ))
    (set! (.-setEditable marker)
          (fn [to-editor]
            (.setVisible popup true)
            (if to-editor
              (let [content (dom/get-text view-text-el)]
                (dom/set-text editor-text-el content)
                (dom/replace-el view-el editor-el))
              (let [content (dom/get-value editor-text-el)]
                (dom/set-text view-text-el content)
                (dom/replace-el editor-el view-el)))))
    (set! (.-setContent popup)
          (fn [content]
            (dom/set-text view-text-el content)))
    popup
    ))

(defn place-marker [id [px py] text]
  (let [item (dom/get-element "work_img_static")
        [item-x item-y] (dom/get-position item)
        [item-width item-height] (dom/get-size item)
        marker (dom/build [:div.marker {:id (or id "new_marker")}])
        _ (dom/add-remove-class marker (work_size) "")
        side (marker-side)
        m-x (+ item-x (min (- item-width side)
                           (Math/floor (* (/ px 100) item-width))))
        m-y (+ item-y (min (- item-height side)
                           (Math/floor (* (/ py 100) item-height))))]
    (dom/prepend (dom/get-element "work")
                 marker)
    (dom/set-position marker m-x m-y)
    (comment (js* "var item = document.getElementById('work_img_static');
var popupElt = document.getElementById('popup');
    var popup = new goog.ui.Popup(popupElt);
 popup.setPosition(new goog.positioning.AnchoredViewportPosition(item,
          3,true));
      popup.setVisible(true);"))
    (set! (.-setNote marker)
          (fn [content]
            (let [marker-id (.-id marker) ;; may have been updated
                  ]
              (send-marker marker-id px py content))))
    (set! (.-setContent marker)
          (fn [content]
            ))
    (let [note (place-note marker text)]
      (set! (.-setContent marker)
          (fn [content]
            (.setContent note content))))
    marker
    ))

(defn place-on-work [e]
  (let [item (.-currentTarget e)
        [item-width item-height] (dom/get-size item)
        [item-x item-y] (dom/get-position item)
        client-x (.-clientX e)
        client-y (.-clientY e)
        d (goog.dom.getDomHelper item)
        scroll (.getDocumentScrollElement d)
        in-item-x (- (+ client-x (.-scrollLeft scroll))
                     item-x)
        in-item-y (- (+ client-y (.-scrollTop scroll))
                     item-y)
        side (marker-side)
        in-item-x (max 0 (- in-item-x (/ side 2)))
        in-item-y (max 0 (- in-item-y (/ side 2)))
        px (Math/floor (* 100 (/ in-item-x item-width)))
        py (Math/floor (* 100 (/ in-item-y item-height)))
        ]
    [px py]))

(defn show-work []
  (let [item (dom/get-element "work_img_static")
        
        _ (set! (.-src item)
                (str "http://www.rijksmuseum.nl/media/assets/" (work_id) (work_img_size)))
        #_(dom/build [:img {:id "work_img"
                               :src "images/herengracht.jpeg"
                               :style "border: 0px solid green;"}])]
    (dom/append (dom/get-element "work") item)
    (let [[item-x item-y] (dom/get-position item)
          [item-width item-height] (dom/get-size item)]
      (events/listen item
                    "click"
                    (fn [e]
                      (doto (place-marker nil (place-on-work e) "Write here...")
                        (.setEditable true))
                      
                      (.preventDefault e)
                      )))))

(defn handler []
  (let [h (goog.net.BrowserChannel.Handler.)]
    (set! (.-channelOpened h)
          (fn [channel]
            (.sendMap channel (doto (js-obj)
                                (aset "type" "watching")
                                (aset "work_id" (work_id))))
            ))
    (set! (.-channelHandleArray h)
          (fn [x data]
            (let [marker-str (or (aget data "marker") "{}")
                  {:keys [id px py content] :as marker} (reader/read-string marker-str)
                  ]
              (if-let [existing-marker (or (dom/get-element (str id))
                                           (dom/get-element "new_marker"))]
                (do
                  (set! (.-id existing-marker) id)
                  (.setContent existing-marker content))
                
                (place-marker id [px py] content))
              )))
    h))

(defn say [text]
  (.sendMap channel (doto (js-obj)
                      (aset "msg" text)) ))

(defn send-marker [id px py content]
  (.sendMap channel (doto (js-obj)
                      (aset "type" "marker")
                      (aset "id" id)
                      (aset "work_id" (work_id))
                      (aset "px" px)
                      (aset "py" py)
                      (aset "content" content))))

(def channel (goog.net.BrowserChannel.))

(defn connect-markers []
  (events/listen js/window "unload" #(do
                                       (.disconnect channel ())
                                       (events/removeAll)))
  (doto (.. channel getChannelDebug getLogger)
      (.setLevel goog.debug.Logger.Level.OFF))
  (doto channel
    (.setHandler (handler))
    (.connect "/channel/test" "/channel/bind")
    ))

(defn init-size-overlay []
  (let [overlay (dom/get-element "size-overlay")
        overlay-img (dom/get-element "work_img_overlay")
        work (dom/get-element "work")
        ]
    (set! (.-width overlay-img) 0)
    (set! (.-height overlay-img) 0)
    (dom/show-element overlay false)))

(defn toggle-size-indicator []
  (let [overlay (dom/get-element "size-overlay")
        overlay-img (dom/get-element "work_img_overlay")
        work (dom/get-element "work")
        work-real-width (.getAttribute work "data_width")
        work-img-width (first (dom/get-size (dom/get-element "work_img_static")))
        compare-to (if (< work-real-width 130)
                     {:img "images/ball.png"
                      :width 100 ;;543
                      :height 100 ;;545
                      :real-width 22
                      :real-cm-per-pixel (/ 22 100)}
                     {:img "images/orange-bike.png"
                      :width 200 ;;660
                      :height 135 ;;444
                      :real-width 170
                      :real-cm-per-pixel (/ 170 200)})
        work-real-cm-per-pixel (/ work-real-width work-img-width)
        img-factor (/ work-real-cm-per-pixel (:real-cm-per-pixel compare-to))
        ;;_ (.log js/console (str "work-real-width" work-real-width "work-img-width" work-img-width "-" work-real-cm-per-pixel "-" (:real-cm-per-pixel compare-to) "-" img-factor))
        
        img-rel-width (/ (:width compare-to) img-factor)
        img-rel-height (/ (:height compare-to) img-factor)]
  (set! (.-src overlay-img)
        (:img compare-to))
  (set! (.-width overlay-img)
        img-rel-width)
  (set! (.-height overlay-img)
        img-rel-height)
    (dom/toggle-show-element (dom/get-element "size-overlay") true)))

(defn float-menu []
  (let [floater (goog.ui.ScrollFloater.)
        menu (dom/get-element "topbar")]
    (.decorate floater menu)))

(defn size-menu []
  (let [;;small (dom/get-element "size-small")
        medium (dom/get-element "size-medium")
        large (dom/get-element "size-large")]
    #_(events/listenOnce small
                       "click"
                       (fn [e]
                         (set! (.-location js/window)
                               (str "?work=" (work_id) "&size=small"))
                         (.preventDefault e)))
    (events/listenOnce medium
                       "click"
                       (fn [e]
                         (set! (.-location js/window)
                               (str "?work=" (work_id) "&size=medium"))
                         (.preventDefault e)))
    (events/listenOnce large
                       "click"
                       (fn [e]
                         (set! (.-location js/window)
                               (str "?work=" (work_id) "&size=large"))
                         (.preventDefault e)))
    (let [how-big (dom/get-element "how-big")]
      (events/listen how-big
                     "click"
                     (fn [e]
                       (toggle-size-indicator)
                       (.preventDefault e))))))

(defn ^:export run []
  (dom/scroll-away-address-bar)
  (float-menu)
  (size-menu)
  (show-work)
  ;; this should follow show-work
  (init-size-overlay)
  (connect-markers))

(defn ^:export connect []
  (repl/connect "http://192.168.1.106:9000/repl" ))

(defn ^:export index []
  (dom/scroll-away-address-bar)
  (float-menu))

