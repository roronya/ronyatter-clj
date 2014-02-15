(ns ronyatter-clj.core
  (:use [ronyatter-clj.twitter]
        [ronyatter-clj.lanterna]))

(defn -main []
  (start-screen)
  (make-TimelineCache)
  (print-TimelineCache)
  (start-TwitterStream)
;  (println-screen (str (get-size-screen)))
;  (input-until-enter)
;  (clear-screen)
;  (input-until-enter)
 )

