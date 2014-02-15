(ns ronyatter-clj.lanterna
  (:require 
   :reload-all
   [lanterna.screen :as screen])
  (:use [clojure.string :only [join]]))

(def x (atom 0))
(def y (atom 0))
(def maxx (atom 0))
(def maxy (atom 0))
(def column-width (atom 40))

(def screen-size (atom [100 50]))
(defn handle-resize [cols rows]
  (reset! screen-size [cols rows]))

(def screen (screen/get-screen :text))

;(def screen (screen/get-screen :swing {:resize-listener handle-resize :cols 100 :rows 40}))

(defn start-screen []
  (screen/start screen)
  (let [size (screen/get-size screen)]
    (reset! maxx (first size))
    (reset! maxy (second size))))

(defn stop-screen []
  (screen/stop screen))

(defn print-screen [x y str]
  (screen/put-string screen x y  str)
  (screen/redraw screen))

(defn printch-screen [ch]
  (screen/put-string screen @x @y  ch)
  (screen/redraw screen)
  (reset! x (inc @x)))

(defn println-screen [str]
  (screen/put-string screen @x @y  str)
  (screen/redraw screen)
  (reset! y (inc @y)))

(defn printcol-screen [col]
  (doseq [x col]
    (println-screen (str x))))

(defn printall-screen [ch]
  (reset! x 0) 
  (reset! y 0)
  (printcol-screen (take @maxy (repeatedly #(join (repeat (* 2 @column-width) ch))))))

(defn clear-screen []
  (printall-screen "*")
  (screen/clear screen)
  (screen/redraw screen)
  (reset! x 0) 
  (reset! y 0))

(defn get-size-screen []
  (screen/get-size screen))

(defn input-until-enter []
  (loop [InputKey (str (screen/get-key-blocking screen))
         InputString ""]
    (if (= InputKey ":enter")
      nil
      (do
        (printch-screen InputKey)
        (recur (str (screen/get-key-blocking screen)) (str InputString InputKey))))))
