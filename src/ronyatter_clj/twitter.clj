(ns ronyatter-clj.twitter
  (:require [clojure.string :as string])
  (:import [twitter4j TwitterFactory TwitterStreamFactory StatusListener StatusAdapter UserStreamListener])
  (:import [twitter4j.conf ConfigurationBuilder])
  (:import [java.util Deque LinkedList Collections])
  (:use [ronyatter-clj.lanterna]))

(def consumerKey "kI2ijJO8ODuWZ819Zrk7Q")
(def consumerSecret "IQ70vDqXVIthJmV70ieb1RNMdpP0YpYj191HgHZjKyM")
(def accessToken "107718872-9Jiv5HhdrOeh3NtAP3Dt1vMVErKe5MWTmVaXRlmZ")
(def accessTokenSecret "jECQWJ0Palrnxd9egf27ywSPgiagE46xMIH9MR9up7xlP")

(defn make-configurationBuilder []
  (-> (doto (new ConfigurationBuilder)
    (.setDebugEnabled true)
    (.setOAuthConsumerKey consumerKey)
    (.setOAuthConsumerSecret consumerSecret)
    (.setOAuthAccessToken accessToken)
    (.setOAuthAccessTokenSecret accessTokenSecret))
    (.build)))

(defn make-twitter []
  (-> (new TwitterFactory (make-configurationBuilder)) (.getInstance)))

(defn make-twitterStream []
  (-> (new TwitterStreamFactory (make-configurationBuilder)) (.getInstance)))

(defn status2tweetmap [status]
  "API叩くとjsonで帰ってくるのでclojureのmapに変換"
  {:name (-> (-> status (.getUser)) (.getName))
   :screenname (str "@" (-> (-> status (.getUser)) (.getScreenName)))
   :text (-> status (.getText))
   :createdat (str (-> status (.getCreatedAt)))})

(defn print-tweet [tweetmap]
  "1つのtweetを綺麗に整形して出力"
    (println (:name tweetmap))
    (println (:screenname tweetmap))
    (println (:text tweetmap))
    (println (:createdat tweetmap)))

(defn get-timeline []
  "APIを叩いて最新20件のtweetをjsonで取得し、clojureのmapのベクタに変換したものを返す"
  (map #(status2tweetmap %1) (-> (make-twitter) (.getHomeTimeline))))

(defn print-timeline []
  "debug用の関数"
  (doseq [tweetmap (get-timeline)]
    (print-tweet tweetmap)))

;;タイムラインをキャッシュしておくキュー
(def timeline-cache (ref clojure.lang.PersistentQueue/EMPTY))

(defn make-timeline-cache []
  "出力する最新20件のtweetを作る"
  (doseq [tweetmapvec (reverse (get-timeline))]
    (dosync (ref-set timeline-cache (conj @timeline-cache tweetmapvec)))))

(defn print-timeline-cache []
  "タイムラインキャッシュを出力"
  (doseq [tweetmap (reverse @timeline-cache)]
    (print-tweet tweetmap)))

(defn print-screen-tweet [tweetmap]
  "1つのtweetを綺麗に整形して出力"
  (println-screen (:name tweetmap))
  (println-screen (:screenname tweetmap))
  (println-screen (:text tweetmap))
  (println-screen (:createdat tweetmap)))

(defn print-screen-timeline-cache []
  "タイムラインキャッシュを出力"
  (clear-screen)
  (doseq [tweetmap (reverse @timeline-cache)]
    (print-screen-tweet tweetmap)))

(defn update-timeline-cache [tweetmap]
  (dosync (ref-set timeline-cache (pop @timeline-cache)))
  (dosync (ref-set timeline-cache (conj @timeline-cache tweetmap))))

;;キューのテスト -> 後ろから入って頭から出る
;(def test (ref (conj clojure.lang.PersistentQueue/EMPTY 10 20 30 40)))
;(dosync (ref-set test (conj @test 1)))
;(dosync (ref-set test (pop @test)))
;(dosync (ref-set test (reverse @test)))
;(doseq [x @test] (println x))

;(make-timeline-cache)
;(println (reverse @timeline-cache))
;(print-timeline-cache)
;(pop @timeline-cache)
;(conj 
;(make-timeline-cache)
;(start-screen)
;(stop-screen)
;(printcol-screen @timeline-cache)
;(clear-screen)
;(print-screen-tweet (first @timeline-cache))

(def listener
  (reify 
    UserStreamListener
    (onStatus [this status]
      (update-timeline-cache (status2tweetmap status))
      (print-screen-timeline-cache))
    (onDeletionNotice [this statusDeletionNotice] nil)
    (onTrackLimitationNotice [this numberOfLimitedStatuses] nil)
    (onScrubGeo [this userId upToStatusId] nil)
    (onStallWarning [this warning] this)
    (onFriendList [this friendIds] nil)
    (onFavorite [this source target favoritedStatus] nil)
    (onUnfavorite [this source rarget favoritedStatus] nil)
    (onFollow [this source followedUser] nil)
    (onDirectMessage [this directMessge])
    (onUserListMemberAddition [this addedMember listOwner alist] nil)
    (onUserListSubscription [this subscriver listOwner alist] nil)
    (onUserListUnsubscription [this subscriber listOwner alist] nil)
    (onUserListCreation [this listOwner alist] nil)
    (onUserListUpdate [this listOwner alist] nil)
    (onUserListDeletion [this listOwner alist] nil)
    (onUserProfileUpdate [this updateUser] nil)
    (onBlock [this source blockedUser] nil)
    (onUnblock [this source unblockedUser] nil)
    (onException [this ex] nil)))

(defn start-twitterStream []
  (let [twitterStream (make-twitterStream)]
    (-> twitterStream (.addListener listener))
    (-> twitterStream (.user))))
