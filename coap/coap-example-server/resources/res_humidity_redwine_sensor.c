#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include<time.h>
#include <stdint.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/node-id.h"

#include "global_variables.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "humidity_redwine-sensor"
#define LOG_LEVEL LOG_LEVEL_APP

static void humidity_redwine_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void humidity_redwine_event_handler(void);

EVENT_RESOURCE(res_humidity_redwine_sensor,
         "title=\"humidity_redwine sensor\"; obs",
         humidity_redwine_get_handler,
         NULL,
         NULL,
         NULL,
	 humidity_redwine_event_handler);

static unsigned int humidity_redwine_level = 65;

static bool update_humidity_redwine () { // simulate the behavior of the real sensor
	bool updated = false;
	unsigned int old_humidity_redwine_level = humidity_redwine_level;

    int value = 0;

	if(dehumidifier_redwine_on) {	// If the dehumidifier system is turned on, humidity decrease
	    value = rand()%3 + 3; // a random number in [3;5]
		humidity_redwine_level = (int) (humidity_redwine_level - value);
	}else{
        value = rand()%3 + 3; // a random number in [3;5]
	    humidity_redwine_level = (int) (humidity_redwine_level + value);	// In any case, the humidity level can only increase more or less rapidly
    }

	if(old_humidity_redwine_level != humidity_redwine_level)
		updated = true;

	return updated;
}

static void humidity_redwine_event_handler(void) {
	if (update_humidity_redwine()) { // if the value is changed
		LOG_INFO("Humidity Red Wine level: %u g/m3\n", humidity_redwine_level);
		// Notify all the observers
    	coap_notify_observers(&res_humidity_redwine_sensor);
	}
}

static void humidity_redwine_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
  	  	char message[64];
      	int length = 64;
      	snprintf(message, length, "{\"concentration\": %d}", (unsigned int) humidity_redwine_level);

      	size_t len = strlen(message);
      	memcpy(buffer, (const void *) message, len);

      	coap_set_header_content_format(response, TEXT_PLAIN);
      	coap_set_header_etag(response, (uint8_t *)&len, 1);
      	coap_set_payload(response, buffer, len);
}