(ns ronyatter-clj.lanterna
  (:require 
   :reload-all
   [lanterna.screen :as screen]))

(def x (ref 0))
(def y (ref 0))
(def screen-size (ref [100 50]))
(defn handle-resize [cols rows]
  (dosync (ref-set screen-size [cols rows])))
(def screen (screen/get-screen :swing {:resize-listener handle-resize}))

(defn start-screen []
  (screen/start screen))

(defn stop-screen []
  (screen/stop screen))

(defn clear-screen []
  (screen/clear screen)
  (screen/redraw screen)
  (dosync (ref-set x 0))
  (dosync (ref-set y 0)))

(defn print-screen [x y str]
  (screen/put-string screen x y str)
  (screen/redraw screen))

(defn println-screen [str]
  (screen/put-string screen @x @y str)
  (screen/redraw screen)
  (dosync (ref-set y (inc @y))))

(defn printcol-screen [col]
  (doseq [x col]
    (println-screen (str x))))

(defn -main []
  (start-screen)
  (printcol-screen @screen-size)
  )

(start-screen)
(screen/get-size screen)
(stop-screen)
@screen-size
