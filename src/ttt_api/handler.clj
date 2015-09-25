(ns ttt-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn strip [coll chars]
  (apply str (remove #((set chars) %) coll)))

(defn count-char [board char]
  (count (strip board char)))

(defn valid-board? [board]
  (cond
    (not (= (count board) 9)) false
    (not (= (count (strip board "ox ")) 0)) false
    :else true))

(defn o-turn? [board]
  (let [diff (- (count-char board "x") (count-char board "o"))]
    (= diff 0)))

(defn row-win? [board]
  (or (and (= (get board 0) (get board 1) (get board 2))
				   (not (= (get board 0) \space)))
      (and (= (get board 3) (get board 4) (get board 5))
				   (not (= (get board 3) \space)))
      (and (= (get board 6) (get board 7) (get board 8))
				   (not (= (get board 6) \space)))))

(defn col-win? [board]
  (or (and (= (get board 0) (get board 3) (get board 6))
				   (not (= (get board 0) \space)))
      (and (= (get board 1) (get board 4) (get board 7))
				   (not (= (get board 1) \space)))
      (and (= (get board 2) (get board 5) (get board 8))
				   (not (= (get board 2) \space)))))

;; TODO
(defn diagonal-win? [board])

(defn won? [board]
  (or (row-win? board) (col-win? board) (diagonal-win? board)))

;; TODO: Row, col, two diagonals.
(defn game-in-progress? [board]
  (cond
    (= (count (strip board "ox")) 0) false ;; draw
    (won? board) false
    :else true))

(defn playable-board? [board]
  (prn board)
  (prn (valid-board? board))
  (prn (o-turn? board))
  (prn (not-finished? board))
  (and
    (valid-board? board)
    (o-turn? board)
    (game-in-progress? board)))

;; Plays in the first random position
(defn play-move [board]
  (clojure.string/replace-first board #" " "o"))

(defn init-board [params]
  (let [board (get params "board")]
    (if (not (playable-board? board))
      {:status 400 :body "Error"}
      (play-move board))))

(defroutes app-routes
  (GET "/" {params :query-params} (init-board params))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
