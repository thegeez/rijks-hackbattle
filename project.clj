(defproject hackbattle "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.1.0-RC1"]
                 [ring/ring-jetty-adapter "1.1.0-RC1"]
                 [org.clojure/clojurescript "0.0-1011" :exclusions [org.clojure/google-closure-library]]
                 [net.thegeez/google-closure-library "0.0-1698"]
                 [net.cgrand/moustache "1.1.0"]
                 [enlive "1.0.0"]
                 [net.thegeez/clj-browserchannel-server "0.0.2"]
                 [net.thegeez/clj-browserchannel-jetty-adapter "0.0.1"]
                 [org.clojure/java.jdbc "0.1.1"]
                 [postgresql "9.1-901.jdbc4"]
                 ]
  )
