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
#define LOG_MODULE "Co2-sensor"
#define LOG_LEVEL LOG_LEVEL_APP

static void Co2_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void Co2_event_handler(void);

EVENT_RESOURCE(res_Co2_sensor,
         "title=\"Co2 sensor\"; obs",
         Co2_get_handler,
         NULL,
         NULL,
         NULL,
	 Co2_event_handler);

static unsigned int Co2_level = 450;

static bool update_Co2 () { // simulate the behavior of the real sensor
	bool updated = false;
	unsigned int old_Co2_level = Co2_level;

    //srand(time(NULL));
    int value = 0;

	if(ventilation_on) {	// If the ventilation system is turned on, air quality improves
	    value = rand()%16 + 15; // a random number in [15;25]
		Co2_level = (int) (Co2_level - value);
	} else if(ventilation_on && alarm){
		value = rand()%16 + 15; // a random number in [15;25]
		Co2_level = (int) (Co2_level - 2*value);
	}else{
	value = rand()%11 + 10; // a random number in [10;20]
	Co2_level = (int) (Co2_level + 2*value);	// In any case, the Co2 level can only increase more or less rapidly
	}	

	if(old_Co2_level != Co2_level)
		updated = true;

	return updated;
}

static void Co2_event_handler(void) {
	if (update_Co2()) { // if the value is changed
		LOG_INFO("Co2 level: %u ppm\n", Co2_level);
		// Notify all the observers
    	coap_notify_observers(&res_Co2_sensor);
	}
}

static void Co2_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
  	  	char message[64];
      	int length = 64;
      	snprintf(message, length, "{\"concentration\": %d}", (unsigned int) Co2_level);

      	size_t len = strlen(message);
      	memcpy(buffer, (const void *) message, len);

      	coap_set_header_content_format(response, TEXT_PLAIN);
      	coap_set_header_etag(response, (uint8_t *)&len, 1);
      	coap_set_payload(response, buffer, len);
}