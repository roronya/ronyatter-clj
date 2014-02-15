(ns ronyatter-clj.twitter
  (:use [clojure.string :only [join]])
  (:import [twitter4j TwitterFactory TwitterStreamFactory StatusListener StatusAdapter UserStreamListener])
  (:import [twitter4j.conf ConfigurationBuilder])
  (:import [java.util Deque LinkedList Collections])
  (:use [ronyatter-clj.lanterna]))

(def consumerKey "kI2ijJO8ODuWZ819Zrk7Q")
(def consumerSecret "IQ70vDqXVIthJmV70ieb1RNMdpP0YpYj191HgHZjKyM")
(def accessToken "107718872-9Jiv5HhdrOeh3NtAP3Dt1vMVErKe5MWTmVaXRlmZ")
(def accessTokenSecret "jECQWJ0Palrnxd9egf27ywSPgiagE46xMIH9MR9up7xlP")

(defn make-ConfigurationBuilder []
  (-> (new ConfigurationBuilder)
      (.setDebugEnabled true)
      (.setOAuthConsumerKey consumerKey)
      (.setOAuthConsumerSecret consumerSecret)
      (.setOAuthAccessToken accessToken)
      (.setOAuthAccessTokenSecret accessTokenSecret)
      (.build)))

(defn make-Twitter []
  (.. (new TwitterFactory (make-ConfigurationBuilder)) getInstance))

(defn make-TwitterStream []
  (.. (new TwitterStreamFactory (make-ConfigurationBuilder)) getInstance))

(def testcase "おねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおねっちゃんおね")

(defn splitstr-at [n str]
  (map #(join %1)(split-at n str)))

(defn split-each [n str]
  (let [returnval (ref [])]
    (loop [result (splitstr-at n str)]
        (dosync (ref-set returnval (conj @returnval (first result))))
        (when-not (= "" (second result))
          (recur (splitstr-at n (second result)))))
    @returnval))

(defn print-Tweet [Tweet]
  "1つのtweetを綺麗に整形して出力"
  (println-screen (str (:name Tweet) ":" (:screenname Tweet)))
  (printcol-screen (split-each 40 (:text Tweet)))
  (println-screen (:createdat Tweet)))

(defn rawdata->Tweet [status]
  "API叩くとjsonで帰ってくるのでclojureのmapに変換
   Tweetの形式は以下のようなmap
   {:nama  ろにゃ
    :screennama @roronya
    :text 今 日 の ミ クちゃんも最高にかわいかった
    :createdat 20130408}"
  {:name (.. status getUser getName)
   :screenname (str "@" (.. status getUser  getScreenName))
   :text (.. status getText)
   :createdat (str (.. status getCreatedAt))})

;;タイムラインをキャッシュしておくキュー
(def TimelineCache (ref clojure.lang.PersistentQueue/EMPTY))

(defn get-Timeline []
  "APIを叩いて最新20件のtweetをjsonで取得し、
   clojureのmapのベクタに変換したものを返す"
  (map #(rawdata->Tweet %1) (.. (make-Twitter) getHomeTimeline)))

(defn make-TimelineCache []
  "出力する最新20件のtweetを作る"
  (doseq [Tweetvec (reverse (get-Timeline))]
    (dosync (ref-set TimelineCache (conj @TimelineCache Tweetvec)))))

(defn print-TimelineCache []
  "タイムラインキャッシュを出力"
  (clear-screen)
  (doseq [Tweet (reverse @TimelineCache)]
    (print-Tweet Tweet)))

(defn update-TimelineCache [Tweet]
  (dosync (ref-set TimelineCache (pop @TimelineCache)))
  (dosync (ref-set TimelineCache (conj @TimelineCache Tweet))))

(def listener
  (reify 
    UserStreamListener
    (onStatus [this status]
      (update-TimelineCache (rawdata->Tweet status))
      (print-TimelineCache))
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

(defn start-TwitterStream []
  (let [TwitterStream (make-TwitterStream)]
    (.. TwitterStream (addListener listener))
    (.. TwitterStream user)))

