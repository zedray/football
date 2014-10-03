#include <pebble.h>

const int timerShort = 10;
const int timerLong = 50;

static Window *window;
static TextLayer *text_layer;
static AppTimer *timer;

static int left = 0;
static int middle = 0;
static int right = 0;

static int message;
static int lastMessage = -1;

static void timer_callback(void *data) {
    timer = NULL;

    // Send the message
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Sending left %d middle %d right %d = %d", left, middle, right, message);
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    Tuplet value = TupletInteger(0, message);
    dict_write_tuplet(iter, &value);
    app_message_outbox_send();

    //Register next execution (if stuff has changed)
    if (lastMessage != message) {
        timer = app_timer_register(timerLong, (AppTimerCallback) timer_callback, NULL);
        lastMessage = message;
    }
}

static void regiserTimer() {
    if (timer == NULL) {
        timer = app_timer_register(timerShort, (AppTimerCallback) timer_callback, NULL);
    }
}

static void updateUi() {
    message = 0;
    if (left == 1) {
        message += 1;
    }
    if (middle == 1) {
        message += 2;
    }
    if (right == 1) {
        message += 4;
    }

    // Actually update the UI.
    static char result_text[] = "x";
    snprintf(result_text, sizeof(result_text), "%d", message);
    text_layer_set_text(text_layer, result_text);

    regiserTimer();
}

static void left_press_click_handler(ClickRecognizerRef recognizer, void *context) {
    left = 1;
    updateUi();
}

static void left_release_click_handler(ClickRecognizerRef recognizer, void *context) {
    left = 0;
    updateUi();
}

static void middle_press_click_handler(ClickRecognizerRef recognizer, void *context) {
    middle = 1;
    updateUi();
}

static void middle_release_click_handler(ClickRecognizerRef recognizer, void *context) {
    middle = 0;
    updateUi();
}

static void right_press_click_handler(ClickRecognizerRef recognizer, void *context) {
    right = 1;
    updateUi();
}

static void right_release_click_handler(ClickRecognizerRef recognizer, void *context) {
    right = 0;
    updateUi();
}

static void click_config_provider(void *context) {
    window_raw_click_subscribe(BUTTON_ID_UP, left_press_click_handler, left_release_click_handler, context);
    window_raw_click_subscribe(BUTTON_ID_SELECT, middle_press_click_handler, middle_release_click_handler, context);
    window_raw_click_subscribe(BUTTON_ID_DOWN, right_press_click_handler, right_release_click_handler, context);
}

static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  text_layer = text_layer_create((GRect) { .origin = { 0, 72 }, .size = { bounds.size.w, 20 } });
  text_layer_set_text(text_layer, "Press a button");
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(text_layer));
}

static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
}

void out_sent_handler(DictionaryIterator *sent, void *context) {
    // outgoing message was delivered
}

void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
    // outgoing message failed
}

void in_received_handler(DictionaryIterator *received, void *context) {
    // incoming message received
}

void in_dropped_handler(AppMessageResult reason, void *context) {
    // incoming message dropped
}

static void init(void) {
    window = window_create();
    window_set_click_config_provider(window, click_config_provider);
    window_set_window_handlers(window, (WindowHandlers) {
      .load = window_load,
      .unload = window_unload,
    });
    window_stack_push(window, true);
    
    app_message_register_inbox_received(in_received_handler);
    app_message_register_inbox_dropped(in_dropped_handler);
    app_message_register_outbox_sent(out_sent_handler);
    app_message_register_outbox_failed(out_failed_handler);

    const uint32_t inbound_size = 64;
    const uint32_t outbound_size = 64;
    app_message_open(inbound_size, outbound_size);
}

static void deinit(void) {
  window_destroy(window);
}

int main(void) {
  init();
  app_event_loop();
  deinit();
}
