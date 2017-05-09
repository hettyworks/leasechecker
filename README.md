# hello-world

FIXME

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

To run via Docker:

	docker build -t oheller/leasechecker .
	docker run --rm -p 3000:3000 oheller/leasechecker
    
## Known Issues

* Only recognizes first capture group
* Uses naive link building method to return image; ideally would be a more robust API call
* Returns links even for nonexistent cards

## License

Copyright © 2017 FIXME
