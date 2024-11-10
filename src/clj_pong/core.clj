(ns clj-pong.core
  (:import [com.badlogic.gdx ApplicationListener Gdx Input$Keys]
           [com.badlogic.gdx.backends.lwjgl3 Lwjgl3Application Lwjgl3ApplicationConfiguration]
           [com.badlogic.gdx.graphics OrthographicCamera Color]
           [com.badlogic.gdx.utils ScreenUtils]
           [com.badlogic.gdx.utils.viewport FitViewport]
           [com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType]))

(def screen-width 800)
(def screen-height 600)
(def paddle-height 100)
(def paddle-width 20)
(def paddle-speed 300)

(def gdx-objects (atom {}))

(def init-state {:ball         {:velocity-x 300.0
                                :velocity-y 300.0
                                :x (* screen-width 0.5)
                                :y 0
                                :radius 10.0}
                 :left-paddle  {:x 5
                                :y (- (* screen-height 0.5) (* paddle-height 0.5))
                                :velocity-x 0
                                :velocity-y 0}
                 :right-paddle {:x (- screen-width paddle-width 5)
                                :y (- (* screen-height 0.5) (* paddle-height 0.5))
                                :velocity-x 0
                                :velocity-y 0}})

(def global-state (atom init-state))

(defn get-delta-time []
  (. Gdx/graphics getDeltaTime))

(defn apply-velocity [entity]
  (let [dx (* (:velocity-x entity) (get-delta-time))
        dy (* (:velocity-y entity) (get-delta-time))]
    (-> entity
        (assoc :x (+ (:x entity) dx))
        (assoc :y (+ (:y entity) dy)))))

(defn collides? [ball paddle]
  (let [{ball-x :x ball-y :y radius :radius} ball
        ball-left (- ball-x radius)
        ball-right (+ ball-x radius)
        ball-top (+ ball-y radius)
        ball-bottom (- ball-y radius)
        {paddle-x :x paddle-y :y} paddle
        paddle-left paddle-x
        paddle-right (+ paddle-x paddle-width)
        paddle-top (+ paddle-y paddle-height)
        paddle-bottom paddle-y]
    (and (>= ball-top paddle-bottom)
         (<= ball-bottom paddle-top)
         (>= ball-right paddle-left)
         (<= ball-left paddle-right))))

(defn apply-ball-collision [ball]
  (let [top (+ (:y ball) (:radius ball))
        bottom (- (:y ball) (:radius ball))]
    (-> ball
        (assoc :velocity-x (cond
                             (collides? ball (:left-paddle @global-state)) (abs (:velocity-x ball))
                             (collides? ball (:right-paddle @global-state)) (- (abs (:velocity-x ball)))
                             :else (:velocity-x ball)))
        (assoc :velocity-y (cond
                             (<= bottom 0) (abs (:velocity-y ball))
                             (>= top screen-height) (- (abs (:velocity-y ball)))
                             :else (:velocity-y ball))))))

(defn update-ball [ball]
  (-> ball
      (apply-velocity)
      (apply-ball-collision)))

(defn update-left-paddle [state]
  (let [paddle (:left-paddle state)
        new-velocity-y (cond
                         (. Gdx/input isKeyPressed Input$Keys/W) (if (>= (+ (:y paddle) paddle-height) screen-height)
                                                                   0
                                                                   paddle-speed)
                         (. Gdx/input isKeyPressed Input$Keys/S) (if (<= (:y paddle) 0)
                                                                   0
                                                                   (- paddle-speed))
                         :else 0)
        new-paddle (->> (assoc paddle :velocity-y new-velocity-y)
                        (apply-velocity))]
    (assoc state :left-paddle new-paddle)))

(defn update-right-paddle [state]
  (let [paddle (:right-paddle state)
        new-velocity-y (cond
                         (. Gdx/input isKeyPressed Input$Keys/UP) (if (>= (+ (:y paddle) paddle-height) screen-height)
                                                                    0
                                                                    paddle-speed)
                         (. Gdx/input isKeyPressed Input$Keys/DOWN) (if (<= (:y paddle) 0)
                                                                      0
                                                                      (- paddle-speed))
                         :else 0)
        new-paddle (->> (assoc paddle :velocity-y new-velocity-y)
                        (apply-velocity))]
    (assoc state :right-paddle new-paddle)))

(defn should-reset? [state]
  (let [{ball-x :x radius :radius} (:ball state)
        ball-left (- ball-x radius)
        ball-right (+ ball-x radius)]
    (or (< ball-right 0)
        (> ball-left screen-width))))

(defn tick [state]
  (if (should-reset? state)
    init-state
    (-> state
        (update-left-paddle)
        (update-right-paddle)
        (assoc :ball (update-ball (:ball state))))))

(defn draw-ball [ball]
  (doto (:shape-renderer @gdx-objects)
    (.setColor Color/RED)
    (.circle (:x ball) (:y ball) (:radius ball)))
  nil)

(defn draw-paddle [paddle]
  (let [{x :x y :y} paddle]
    (doto (:shape-renderer @gdx-objects)
      (.setColor Color/TAN)
      (.rect x y paddle-width paddle-height))))

(defn draw [state]
  (draw-ball (:ball state))
  (draw-paddle (:left-paddle state))
  (draw-paddle (:right-paddle state))
  nil)

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
