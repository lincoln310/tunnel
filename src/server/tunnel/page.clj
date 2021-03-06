(ns tunnel.page
  "页面"
  (:require [hiccup.page :refer [html5 include-css include-js]]
            [ring.util.response :as resp :refer [redirect]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

;; =============================================================================
;; Helpers

(defn has-uid? [req]
  (let [uid (-> req :session :uid)]
    (not (nil? uid))))

;; =============================================================================
;; Pages

(defn index-page
  "主页面, 判断"
  [req]
  (if (has-uid? req)
    (html5
      [:head
       [:meta {"charset" "utf-8"}]
       [:meta {"name" "viewport"
               "content" "width=device-width, initial-scale=1, maximum-scale=1"}]
       (include-css
         "//cdn.bootcss.com/animate.css/3.5.1/animate.min.css"
         "//cdn.bootcss.com/highlight.js/9.2.0/styles/github-gist.min.css"
         "//cdn.bootcss.com/font-awesome/4.5.0/css/font-awesome.min.css")]
      [:body
       (anti-forgery-field)
       [:div#app]
       (include-js "//cdn.bootcss.com/marked/0.3.5/marked.min.js"
         "//cdn.bootcss.com/highlight.js/9.2.0/highlight.min.js"
         "//cdn.bootcss.com/highlight.js/9.2.0/languages/java.min.js")
       (include-js "js/compiled/tunnel.js")])
    (redirect "/login")))

(defn register-page
  [req]
  (html5
    [:head
     [:meta {"charset" "utf-8"}]
     [:meta {"name" "viewport"
                   "content" "width=device-width, initial-scale=1, maximum-scale=1"}]]
    [:body
     [:h3 "测试, 不要用惯用密码."]
     [:form {:method :POST :action "/api/register"}
      [:input {:name :username :type :text}]
      [:input {:name :password :type :password}]
      [:input {:type :submit :value "注册"}]
      (anti-forgery-field)]
     [:a {:href "/login"} "返回登陆"]]))

(defn login-page
  [req]
  (html5
    [:head
     [:meta {"charset" "utf-8"}]
     [:meta {"name" "viewport"
                   "content" "width=device-width, initial-scale=1, maximum-scale=1"}]]
    [:body
     [:form {:method :POST :action "/api/login"}
      [:input {:name :username :type :text :placeholder "用户名"}]
      [:input {:name :password :type :password}]
      [:input {:type :submit :value "登陆"}]
      (anti-forgery-field)]
     [:a {:href "/register"} "注册(测试)"]]))

(defn error-page
  [req err]
  (html5
    [:head
     [:meta {"charset" "utf-8"}]
     [:meta {"name" "viewport"
             "content" "width=device-width, initial-scale=1, maximum-scale=1"}]]
    [:body
     [:h3 "发生错误了"]
     [:p err]
     [:a {:href "javascript:history.back()"} "返回上一页"]]))
