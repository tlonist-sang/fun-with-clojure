(ns core
  (:require [clj-http.client :as http-client]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [java-time.api :as jt]))

(def api-endpoint "https://api.github.com/graphql")
(def authorization (-> (slurp "resources/config.edn")
                       edn/read-string
                       :github-key))

(defn fetch-github-data [input]
  (-> (http-client/post api-endpoint
                        {:headers {"Authorization" authorization}
                         :body    (json/write-str {:query input})})
      :body
      (json/read-str :key-fn keyword)))

(defn iso8601-str->utc-ldt
  "timestamp string to java date"
  [iso8601-str]
  (let [iso-with-utc (-> (jt/formatter :iso-date-time)
                         (jt/with-zone  "UTC"))]
    (-> (jt/zoned-date-time iso-with-utc iso8601-str)
        jt/local-date-time)))

(defn diff-create-merge-time [{:keys [mergedAt createdAt]} criteria]
  (jt/time-between (iso8601-str->utc-ldt createdAt)
                   (iso8601-str->utc-ldt mergedAt) criteria))

(comment
  (iso8601-str->utc-ldt "2022-10-27T06:55:48Z")
  (->> (slurp "resources/github.graphql")
       (fetch-github-data))

  (let [info (->> (slurp "resources/githubfe.graphql")
                  (fetch-github-data)
                  :data :search :edges
                  (map :node))
        cnt (count info)]
    (->> info
         (map #(diff-create-merge-time % :minutes))
         (reduce +)
         (* (/ cnt) (/ 60.0))))
  (count "00d87142-5f33-452f-a91e-a92fa2d60eaf")
  ;95.9 hr 2022-05-01~2022-06-01
  ;23.9 hr 2022-05-01~2022-06-01

  (jt/local-date-time)
  (jt/time-between (iso8601-str->utc-ldt "2022-10-27T06:16:52Z")
                   (iso8601-str->utc-ldt "2022-11-01T06:03:51Z")
                   :hours))