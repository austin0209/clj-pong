(ns clj-pong.core
  (:import [com.badlogic.gdx ApplicationListener Gdx]
           [com.badlogic.gdx.backends.lwjgl3 Lwjgl3Application Lwjgl3ApplicationConfiguration]
           [com.badlogic.gdx.graphics OrthographicCamera Color]
           [com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType]))

(def gdxObjects (atom {}))

(defn app-listener []
  (reify ApplicationListener
    (create [this]
      (.setTitle Gdx/graphics "clj-pong")
      (reset! gdxObjects {:shape-renderer (ShapeRenderer/new)
                          :camera (OrthographicCamera/new)}))
    (render [this]
      (doto (:shape-renderer @gdxObjects)
        (.setProjectionMatrix (.-combined (:camera @gdxObjects)))
        (.begin (ShapeRenderer$ShapeType/Line))
        (.setColor Color/RED)
        (.rect 0 0 100 100)
        (.end)))
    (pause [this])
    (resume [this])
    (resize [this width height])
    (dispose[this])))

(defn -main []
  (let [config (doto (Lwjgl3ApplicationConfiguration/new)
                 (.setResizable false)
                 (.setWindowedMode 800 600))]
    (Lwjgl3Application. (app-listener) config)))
