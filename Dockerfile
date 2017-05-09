FROM clojure:lein

COPY . app/

WORKDIR app/

RUN lein deps

EXPOSE 3000

CMD ["lein", "ring", "server"]