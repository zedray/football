#include <pebble.h>

static Window *window;
static TextLayer *text_layer;
static int select = 0;
static int up = 0;
static int down = 0;

static void updateUi() {
    int result = 0;
    if (select == 1) {
        result += 1;
    }
    if (up == 1) {
        result += 2;
    }
    if (down == 1) {
        result += 4;
    }
    static char result_text[] = "x";
    snprintf(result_text, sizeof(result_text), "%d", result);
    text_layer_set_text(text_layer, result_text);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Button select %d up %d down %d for %d", select, up, down, result);
    
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    Tuplet value = TupletInteger(0, result);
    dict_write_tuplet(iter, &value);
    app_message_outbox_send();
}

static void select_press_click_handler(ClickRecognizerRef recognizer, void *context) {
    select = 1;
    updateUi();
}

static void select_release_click_handler(ClickRecognizerRef recognizer, void *context) {
    select = 0;
    updateUi();
}

static void up_press_click_handler(ClickRecognizerRef recognizer, void *context) {
    up = 1;
    updateUi();
}

static void up_release_click_handler(ClickRecognizerRef recognizer, void *context) {
    up = 0;
    updateUi();
}

static void down_press_click_handler(ClickRecognizerRef recognizer, void *context) {
    down = 1;
    updateUi();
}

static void down_release_click_handler(ClickRecognizerRef recognizer, void *context) {
    down = 0;
    updateUi();
}

static void click_config_provider(void *context) {
    window_raw_click_subscribe(BUTTON_ID_SELECT, select_press_click_handler, select_release_click_handler, context);
    window_raw_click_subscribe(BUTTON_ID_UP, up_press_click_handler, up_release_click_handler, context);
    window_raw_click_subscribe(BUTTON_ID_DOWN, down_press_click_handler, down_release_click_handler, context);
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
