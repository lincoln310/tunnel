(ns tunnel.core
  (:require [hiccup.core :as html :refer [html]]
            [reloaded.repl :refer [system]]
            [hiccup.page :refer [include-js include-css]]
            [org.httpkit.server]
            [ring.middleware
             [defaults]
             [session]
             [keyword-params]
             [params]
             [stacktrace]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [tunnel.handler :as hdlr]
            [compojure.core :refer [defroutes GET POST]]))

;; =============================================================================
;; Pages

(defn index-page
  [req]
  (html
    [:head]
    [:body
     [:div#app]
     (include-js "js/compiled/tunnel.js")]))

(defn login-page
  [req]
  (html
    [:head
     (include-css "css/normalize.css")]
    [:body
     [:form {:method :POST :action "/api/login"}
      [:input {:name :username :type :text :placeholder "用户名"}]
      [:input {:name :password :type :password}]
      [:input {:type :submit :value "登陆"}]
      (anti-forgery-field)]]))

(defroutes route
  (GET "/" req index-page)
  (GET "/login" req login-page)
  (POST "/api/:key" req hdlr/api-handler*)
  (GET "/chsk" req ((-> system :sente :ring-ajax-get-or-ws-handshake) req))
  (POST "/chsk" req ((-> system :sente :ring-ajax-post) req)))

(def ring-handler (-> #'route
                    ;; 先用最简单的session方案.
                    ;; ring.middleware.session/wrap-session
                    (ring.middleware.defaults/wrap-defaults
                      ring.middleware.defaults/site-defaults)
                    ring.middleware.stacktrace/wrap-stacktrace
                    ))

#_(org.httpkit.server/run-server ring-handler
  {:port 3456})
