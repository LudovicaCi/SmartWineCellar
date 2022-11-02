#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "dehumidifier_redwine-system"
#define LOG_LEVEL LOG_LEVEL_APP

static void dehumidifier_redwine_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_dehumidifier_redwine_system,
         "title=\"Dehumidifier Red Wine System\";rt=\"Control\"",
         NULL,
         NULL,
         dehumidifier_redwine_put_handler,
         NULL);

bool dehumidifier_redwine_on = false;

static void dehumidifier_redwine_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
	size_t len = 0;
	const char *text = NULL;
	char mode[4];
	memset(mode, 0, 3);

	int mode_success = 1;

	len = coap_get_post_variable(request, "mode", &text);
	if(len > 0 && len < 4) {
		memcpy(mode, text, len);
		if(strncmp(mode, "ON", len) == 0) {
			dehumidifier_redwine_on = true;
			leds_single_off(LEDS_GREEN);
			leds_single_off(LEDS_RED);
			leds_on(LEDS_RED);
			LOG_INFO("Dehumidifier Red Wine System ON\n");
		} else if(strncmp(mode, "OFF", len) == 0) {
			dehumidifier_redwine_on = false;
			leds_single_off(LEDS_GREEN);
			leds_single_off(LEDS_RED);
			leds_on(LEDS_GREEN);
			LOG_INFO("Dehumidifier Red Wine System OFF\n");
		} else {
			mode_success = 0;
		}
	} else {
		mode_success = 0;
	}
	
	if(!mode_success) {
    		coap_set_status_code(response, BAD_REQUEST_4_00);
 	}
}
