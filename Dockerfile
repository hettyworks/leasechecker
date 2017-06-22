FROM clojure:lein

COPY . app/
WORKDIR app/
RUN lein deps

CMD ["lein", "run"]