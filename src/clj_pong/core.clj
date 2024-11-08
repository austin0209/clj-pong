(ns clj-pong.core
  "Clj Pong"
  (:import [com.raylib Raylib Raylib$Camera2D]
           [com.raylib Jaylib])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (Raylib/InitWindow 400 400 "Demo")
  (Raylib/SetTargetFPS 60)
  (loop [should-close (Raylib/WindowShouldClose)
         camera (Raylib$Camera2D/new)]
    (if should-close
      (Raylib/CloseWindow)
      (do
        (Raylib/BeginDrawing)
        (Raylib/ClearBackground Jaylib/RAYWHITE)
        (Raylib/BeginMode2D camera)
        (Raylib/DrawGrid 20 1.0)
        (Raylib/EndMode2D)
        (Raylib/DrawText "Hello world!" 190 200 20 Jaylib/VIOLET)
        (Raylib/DrawFPS 20 20)
        (Raylib/EndDrawing)
        (recur (Raylib/WindowShouldClose) camera)))))
