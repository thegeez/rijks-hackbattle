# hackbattle

Entry for The Next Web hackbattle, using the Rijksmuseum API [0] and
browserchannel [1].

[0]: http://www.rijksmuseum.nl/api
[1]: https://github.com/thegeez/clj-browserchannel

## License

Copyright © 2012 Gijs Stuurman / @thegeez / http://thegeez.net

heroku create --stack cedar --buildpack https://github.com/heroku/heroku-buildpack-clojure

heroku labs:enable user_env_compile -a empty-planet-8852
heroku config:add LEIN_BUILD_TASK="run -m tasks.build-development-js"
heroku domains:add rijks.thegeez.net
