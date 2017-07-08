FROM clojure:lein

COPY project.clj app/
WORKDIR app/
RUN lein deps

WORKDIR ../
COPY . app/
WORKDIR app/


CMD ["lein", "run"]