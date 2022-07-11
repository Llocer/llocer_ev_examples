#/bin/bash

URL="http://127.0.0.1:8080/llocer_cso"
HEADERS=(-H 'Authorization: Token AOAM' -H 'X-Request-ID: oamRequestId' -H 'X-Correlation-ID: oamCorrelationId' -H 'OCPI-to-country-code: ES' -H 'OCPI-to-party-id: MCS') 

curl -X PUT "${HEADERS[@]}" "$URL/221/locations/ES/MCS/location1/" -d @- <<END
{
	"country_code": "ES",
	"party_id": "MCS",
	"id": "location1",
	"publish": true,
	"address": "Sesame street",
	"country": "ESP",
	"coordinates": { "latitude": "my latitude", "longitude": "my longitude" },
	"time_zone": "Europe/Madrid"
}
END
