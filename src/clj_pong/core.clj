(ns clj-pong.core
  (:import [com.badlogic.gdx ApplicationListener Gdx]
           [com.badlogic.gdx.backends.lwjgl3 Lwjgl3Application Lwjgl3ApplicationConfiguration]
           [com.badlogic.gdx.graphics OrthographicCamera Color]
           [com.badlogic.gdx.utils ScreenUtils]
           [com.badlogic.gdx.utils.viewport FitViewport]
           [com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType]))

(def screen-width 800)
(def screen-height 600)

(def gdx-objects (atom {}))

(def init-state {:entities [{:type :ball
                             :velocity-x 300.0
                             :velocity-y 300.0
                             :x (* screen-width 0.5)
                             :y (* screen-height 0.5)
                             :radius 10.0}]})

(def global-state (atom init-state))

(defn get-delta-time []
  (. Gdx/graphics getDeltaTime))

(defn applyVelocity [entity]
  (let [dx (* (:velocity-x entity) (get-delta-time))
        dy (* (:velocity-y entity) (get-delta-time))]
    (-> entity
        (assoc :x (+ (:x entity) dx))
        (assoc :y (+ (:y entity) dy)))))

(defn applyBallCollision [ball]
  (let [left (- (:x ball) (:radius ball))
        right (+ (:x ball) (:radius ball))
        top (+ (:y ball) (:radius ball))
        bottom (- (:y ball) (:radius ball))]
    (-> ball
        (assoc :velocity-x (cond
                             (<= left 0) (abs (:velocity-x ball))
                             (>= right screen-width) (- (abs (:velocity-x ball)))
                             :else (:velocity-x ball)))
        (assoc :velocity-y (cond
                             (<= bottom 0) (abs (:velocity-y ball))
                             (>= top screen-height) (- (abs (:velocity-y ball)))
                             :else (:velocity-y ball))))))

(defn update-ball [ball]
  (-> ball
      (applyVelocity)
      (applyBallCollision)))

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
        (.set (.-position camera) (* screen-width 0.5) (* screen-height 0.5) 0)
        (reset! gdx-objects {:shape-renderer (ShapeRenderer/new)
                             :camera camera
                             :viewport (FitViewport/new screen-width screen-height camera)})))
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
                 (.setWindowedMode screen-width screen-height)
                 (.setForegroundFPS 60))]
    (Lwjgl3Application. (app-listener) config)))

(comment
  (future (-main))
  (@global-state)
  (. Gdx/graphics getDeltaTime)
  (reset! global-state init-state)
  (tick init-state)
  (.set (.-position (:camera @gdx-objects)) 0.0 0.0 0.0))
