(ns ronyatter-clj.core
  (:require clojure.string)
  (:import [twitter4j TwitterFactory TwitterStreamFactory StatusListener StatusAdapter UserStreamListener])
  (:import [twitter4j.conf ConfigurationBuilder]))

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

(comment (defn -main []
  (let [twitter (make-twitter)]
    (-> twitter (.getHomeTimeline)))))

(defn -main []
  (let [listener (reify 
                   UserStreamListener
                   (onStatus [this status]
                     (println (-> (-> status (.getUser)) (.getScreenName)))
                     (println (-> status (.getText)))
                     (println))
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
                   (onException [this ex] nil))
        twitterStream (make-twitterStream)]
    (-> twitterStream (.addListener listener))
    (-> twitterStream (.user))))

(-main)
