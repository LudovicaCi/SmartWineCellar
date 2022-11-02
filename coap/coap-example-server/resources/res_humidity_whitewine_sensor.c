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
#define LOG_MODULE "humidity_whitewine-sensor"
#define LOG_LEVEL LOG_LEVEL_APP

static void humidity_whitewine_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void humidity_whitewine_event_handler(void);

EVENT_RESOURCE(res_humidity_whitewine_sensor,
         "title=\"humidity_whitewine sensor\"; obs",
         humidity_whitewine_get_handler,
         NULL,
         NULL,
         NULL,
	 humidity_whitewine_event_handler);

static unsigned int humidity_whitewine_level = 75;

static bool update_humidity_whitewine () { // simulate the behavior of the real sensor
	bool updated = false;
	unsigned int old_humidity_whitewine_level = humidity_whitewine_level;

    int value = 0;

	if(dehumidifier_whitewine_on) {	// If the dehumidifier system is turned on, humidity decrease
	    value = rand()%3 + 3; // a random number in [3;5]
		humidity_whitewine_level = (int) (humidity_whitewine_level - value);
	}else{
	   value = rand()%3 + 3; // a random number in [3;5]
	   humidity_whitewine_level = (int) (humidity_whitewine_level + value);	// In any case, the humidity level can only increase more or less rapidly
    }

	if(old_humidity_whitewine_level != humidity_whitewine_level)
		updated = true;

	return updated;
}

static void humidity_whitewine_event_handler(void) {
	if (update_humidity_whitewine()) { // if the value is changed
		LOG_INFO("Humidity White Wine level: %u g/m3\n", humidity_whitewine_level);
		// Notify all the observers
    	coap_notify_observers(&res_humidity_whitewine_sensor);
	}
}

static void humidity_whitewine_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
  	  	char message[64];
      	int length = 64;
      	snprintf(message, length, "{\"concentration\": %d}", (unsigned int) humidity_whitewine_level);

      	size_t len = strlen(message);
      	memcpy(buffer, (const void *) message, len);

      	coap_set_header_content_format(response, TEXT_PLAIN);
      	coap_set_header_etag(response, (uint8_t *)&len, 1);
      	coap_set_payload(response, buffer, len);
}