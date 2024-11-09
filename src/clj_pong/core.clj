(ns clj-pong.core
  (:import [com.badlogic.gdx ApplicationListener]
           [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [com.badlogic.gdx.graphics OrthographicCamera Color]
           [com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType]))

(def gdxObjects (atom {}))

(defn app-listener []
  (reify ApplicationListener
    (create [this]
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
  (LwjglApplication. (app-listener) "demo" 800 600))
