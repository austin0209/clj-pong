(ns clj-pong.core
  (:import [com.badlogic.gdx ApplicationListener Gdx]
           [com.badlogic.gdx.backends.lwjgl3 Lwjgl3Application Lwjgl3ApplicationConfiguration]
           [com.badlogic.gdx.graphics OrthographicCamera Color]
           [com.badlogic.gdx.utils ScreenUtils]
           [com.badlogic.gdx.utils.viewport FitViewport]
           [com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType]))

(def gdx-objects (atom {}))

(def init-state {:entities [{:type :ball
                             :velocity-x 300.0
                             :velocity-y 300.0
                             :x 0.0
                             :y 0.0
                             :radius 10.0}]})

(def global-state (atom init-state))

(defn get-delta-time []
  (. Gdx/graphics getDeltaTime))

(defn update-ball [ball]
  (let [dx (* (:velocity-x ball) (get-delta-time))
        dy (* (:velocity-y ball) (get-delta-time))]
    (-> ball
        (assoc :x (+ (:x ball) dx))
        (assoc :y (+ (:y ball) dy)))))

(defn update-entity [entity]
  (case (:type entity)
    :ball (update-ball entity)
    entity))

(defn tick [state]
  (-> state
      (assoc :entities (mapv #(update-entity %) (:entities state)))))

(defn draw-ball [ball]
  (doto (:shape-renderer @gdx-objects)
    (.setColor Color/RED)
    (.circle (:x ball) (:y ball) (:radius ball)))
  nil)

(defn draw [state]
  (doall (map #(case (:type %)
                 :ball (draw-ball %)
                 nil)
              (:entities state))))

(defn app-listener []
  (reify ApplicationListener
    (create [_]
      (.setTitle Gdx/graphics "clj-pong")
      (reset! global-state init-state)
      (let [camera (OrthographicCamera/new)]
        (reset! gdx-objects {:shape-renderer (ShapeRenderer/new)
                             :camera camera
                             :viewport (FitViewport/new 800 600 camera)})))
    (render [_]
      (swap-vals! global-state tick)
      (ScreenUtils/clear Color/SKY)
      (doto (:shape-renderer @gdx-objects)
        (.setProjectionMatrix (.-combined (:camera @gdx-objects)))
        (.begin (ShapeRenderer$ShapeType/Filled)))
      (draw @global-state)
      (. (:shape-renderer @gdx-objects) end))
    (pause [_])
    (resume [_])
    (resize [_ width height]
      (. (:viewport @gdx-objects) update width height))
    (dispose [_])))

(defn -main []
  (let [config (doto (Lwjgl3ApplicationConfiguration/new)
                 (.setResizable false)
                 (.setWindowedMode 800 600)
                 (.setForegroundFPS 60))]
    (future (Lwjgl3Application. (app-listener) config))))

(comment
  (-main)
  (@global-state)
  (. Gdx/graphics getDeltaTime)
  (reset! global-state init-state))
