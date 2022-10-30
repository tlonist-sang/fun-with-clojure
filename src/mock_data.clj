(ns mock-data)

(def country ["Korea" "Japan" "China" "USA" "Canada" "Mexico" "Germany" "France" "Spain" "Argentina" "Brazil" "Chile"])
(def first-name ["Kevin" "Sanghyun" "Kai" "Garry" "Alfonzo" "Elisabeth" "Charles" "Maurice" "Keiko"])
(def last-name ["Kim" "Li" "Zhang" "Jackson" "Howard" "Scott" "Halpert" "Schrute" "Carrie"])
(def language ["Korean" "Spanish" "English" "Japanese" "Chinese" "Portuguese" "French" "German"])
(defn generate [iterate-func n]
  (->> (iterate #(conj % (iterate-func)) [])
       (take n)
       last))

(defn generate-data
  "removes total duplicate"
  [n]
  (let [countries (generate #(rand-nth country) n)
        name (generate #(str (rand-nth first-name) " " (rand-nth last-name)) n)
        age (generate #(+ 20 (rand-int 30)) n)
        language (generate (fn []
                             (let [primary (rand-nth language)
                                   secondary (loop [s (rand-nth language)]
                                               (if (not= s primary) s (recur (rand-nth language))))]
                               {"primary"   primary
                                "secondary" secondary})) n)]
    (-> (map (fn [a b c d] (assoc {} :country a :name b :age c :language d)) countries name age language)
        distinct)))



(comment
  (def mock-data (generate-data 1000))
  (count mock-data)
  (->> (generate-data 100)
       (map :country)
       frequencies))