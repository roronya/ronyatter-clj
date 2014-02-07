(ns ronyatter-clj.core
  (:use [ronyatter-clj.twitter]
        [ronyatter-clj.lanterna]))

(defn -main []
  (start-screen)
  (make-timeline-cache)
  (start-twitterStream)
)

