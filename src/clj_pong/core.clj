(ns clj-pong.core
  "Clj Pong"
  (:import [com.raylib Raylib Raylib$Camera2D]
           [com.raylib Jaylib Jaylib$Vector2])
  (:gen-class))

(defn tick
  [state]
  (let [new-entities (mapv #(or ((:update-fn %) %) %) (:entities state))]
    (assoc state :entities new-entities)))

(defn draw
  [state]
  (Raylib/BeginDrawing)
  (Raylib/ClearBackground Jaylib/RAYWHITE)
  (Raylib/BeginMode2D (:camera state))
  (Raylib/DrawCircle 0 0 100.0 Jaylib/GREEN)
  (Raylib/EndMode2D)
  (Raylib/DrawText "Hello" 190 200 20, Jaylib/VIOLET)
  (Raylib/DrawFPS 20 20)
  (Raylib/EndDrawing)
  state)

(def init-entities
  [{:name "Ball"
    :velocity-x 0.0
    :velocity-y 0.0
    :x 0
    :y 0
    :update-fn #(-> %
                    (assoc :x (+ (:x %) (:velocity-x %)))
                    (assoc :y (+ (:y %) (:velocity-y %))))
    :draw-fn #()}]);Raylib/DrawCircle 0 0 100.0 Jaylib/VIOLET)}]) ;(:x %) (:y %) 500.0 Jaylib/VIOLET)}])

(def init-state {:entities init-entities
                 :camera (doto
                           (Raylib$Camera2D/new)
                           (.offset (Jaylib$Vector2/new 200.0 200.0))
                           (.zoom 1.0))})

(defn start-game
  []
  (Raylib/InitWindow 400 400 "Demo")
  (Raylib/SetTargetFPS 60)
  (loop [should-close (Raylib/WindowShouldClose)
         state init-state]
    (if should-close
      (Raylib/CloseWindow)
      (->> state
          (tick)
          (draw)
          (recur (Raylib/WindowShouldClose))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (future (start-game)))

(comment
  (-main)
  (Raylib/SetTargetFPS 120)
  (:camera init-state))

