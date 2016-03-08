(ns tunnel.socket
  "和websocket相关的代码."
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)]))


;; =============================================================================
;; WebSocket Init
(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk"
        {:type :auto                    ; e/o #{:auto :ajax :ws}
         })]
  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

;; event channel, 在websocket连接成功之后开始消费.
;; 由于UI会在页面加载的时候立刻开始渲染, 而websocket需要等待连接成功.
;; 所以UI产生的需要websocket处理的事件统一发送到这个channel.
(defonce ch-ev (chan))

(defmulti event-msg-handler
  (fn [ev-id ev-msg] ev-id))

(defn send-ev!
  [ev]
  (put! ch-ev ev))


;; (send-ev! [:msg/test {:text "hello, world"}])

(defn consume-ch-ev
  "消费ch-ev中的事件,
  TODO 使用websocket发送给服务器."
  []
  (go-loop []
    (let [ev (<! ch-ev)]
      (chsk-send! ev)
      (recur))))

(defmethod event-msg-handler :chsk/state
  [ev-id ev-msg]
  (let [{:keys [open? first-open?]} ev-msg]
    (when (and open? first-open?)
      (consume-ch-ev))))

(defmethod event-msg-handler :default
  [_ _])

(defn event-msg-handler*
  [{:keys [event]}]
  (event-msg-handler (first event) (second event)))

;; 用event-msg-handler代替prn, event-msg-handler: (event)
(sente/start-client-chsk-router! ch-chsk
  event-msg-handler*)
