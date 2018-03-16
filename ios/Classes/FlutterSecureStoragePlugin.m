#import "FlutterSecureStoragePlugin.h"

static NSString *const KEYCHAIN_SERVICE = @"flutter_secure_storage_service";
static NSString *const CHANNEL_NAME = @"plugins.it_nomads.com/flutter_secure_storage";

@interface FlutterSecureStoragePlugin()

@property (strong, nonatomic) NSDictionary *query;

@end

@implementation FlutterSecureStoragePlugin

- (instancetype)init {
    self = [super init];
    if (self){
        self.query = @{
                       (__bridge id)kSecClass :(__bridge id)kSecClassGenericPassword,
                       (__bridge id)kSecAttrService :KEYCHAIN_SERVICE,
                       };
    }
    return self;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:CHANNEL_NAME
                                     binaryMessenger:[registrar messenger]];
    FlutterSecureStoragePlugin* instance = [[FlutterSecureStoragePlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (NSString *)getKey:(NSDictionary *)args result:(FlutterResult)result {
    NSString *key = args[@"key"];
    if (![key isKindOfClass:[NSString class]]) {
        result([FlutterError errorWithCode:@"argument_error"
                                   message:@"key must be a string"
                                   details:nil]);
        return nil;
    }
    return key;
}

- (NSArray<NSString *> *)getKeys:(NSDictionary *)args result:(FlutterResult)result {
    NSArray<NSString *> *keys = args[@"keys"];
    if (![keys isKindOfClass:[NSArray class]]) {
        result([FlutterError errorWithCode:@"argument_error"
                                   message:@"keys must be an array"
                                   details:nil]);
        return nil;
    }

    for (NSString *key in keys) {
        if (![key isKindOfClass:[NSString class]]) {
            result([FlutterError errorWithCode:@"argument_error"
                                       message:@"all keys must be strings"
                                       details:nil]);
            return nil;
        }
    }

    return keys;
}

- (NSString *)getValue:(NSDictionary *)args result:(FlutterResult)result {
    NSString *value = args[@"value"];
    if (![value isKindOfClass:[NSString class]]) {
        result([FlutterError errorWithCode:@"argument_error"
                                   message:@"value must be a string"
                                   details:nil]);
        return nil;
    }
    return value;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if (call.arguments == nil) {
        result([FlutterError errorWithCode:@"no_arguments"
                                   message:@"No arguments"
                                   details:nil]);
        return;
    }

    NSDictionary *args = call.arguments;

    if ([@"read" isEqualToString:call.method]) {
        NSString *key = [self getKey:args result:result];
        if (key == nil) return;

        NSString *value = [self read:key];

        result(value);
    } else
    if ([@"write" isEqualToString:call.method]) {
        NSString *key = [self getKey:args result:result];
        if (key == nil) return;
        NSString *value = [self getValue:args result:result];
        if (value == nil) return;

        [self write:value forKey:key];

        result(nil);
    } else
    if ([@"delete" isEqualToString:call.method]) {
        NSString *key = [self getKey:args result:result];
        if (key == nil) return;

        [self delete:key];

        result(nil);
    } else
    if ([@"readAll" isEqualToString:call.method]) {
        NSArray<NSString *> *keys = [self getKeys:args result:result];
        if (keys == nil) return;

        NSArray<NSString *> *values = [self readAll:keys];

        result(values);
    } else
        result(FlutterMethodNotImplemented);
}

- (void)write:(NSString *)value forKey:(NSString *)key {
    NSMutableDictionary *search = [self.query mutableCopy];
    search[(__bridge id)kSecAttrAccount] = key;
    search[(__bridge id)kSecMatchLimit] = (__bridge id)kSecMatchLimitOne;
    
    OSStatus status;
    status = SecItemCopyMatching((__bridge CFDictionaryRef)search, NULL);
    if (status == noErr){
        search[(__bridge id)kSecMatchLimit] = nil;
        
        NSDictionary *update = @{(__bridge id)kSecValueData: [value dataUsingEncoding:NSUTF8StringEncoding]};
        
        status = SecItemUpdate((__bridge CFDictionaryRef)search, (__bridge CFDictionaryRef)update);
        if (status != noErr){
            NSLog(@"SecItemUpdate status = %d", status);
        }
    }else{
        search[(__bridge id)kSecValueData] = [value dataUsingEncoding:NSUTF8StringEncoding];
        search[(__bridge id)kSecMatchLimit] = nil;
        
        status = SecItemAdd((__bridge CFDictionaryRef)search, NULL);
        if (status != noErr){
            NSLog(@"SecItemAdd status = %d", status);
        }
    }
}

- (NSString *)read:(NSString *)key {
    NSMutableDictionary *search = [self.query mutableCopy];
    search[(__bridge id)kSecAttrAccount] = key;
    search[(__bridge id)kSecReturnData] = (__bridge id)kCFBooleanTrue;
    
    CFDataRef resultData = NULL;
    
    OSStatus status;
    status = SecItemCopyMatching((__bridge CFDictionaryRef)search, (CFTypeRef*)&resultData);
    NSString *value;
    if (status == noErr){
        NSData *data = (__bridge NSData*)resultData;
        value = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        
    }
    
    return value;
}

- (NSArray<NSString *> *)readAll:(NSArray<NSString *> *)keys {
    NSMutableArray<NSString *> *values = [NSMutableArray new];

    for (NSString *key in keys) {
        id value = [self read:key] ?: [NSNull null];
        [values addObject:value];
    }

    return values;
}

- (void)delete:(NSString *)key {
    NSMutableDictionary *search = [self.query mutableCopy];
    search[(__bridge id)kSecAttrAccount] = key;
    search[(__bridge id)kSecReturnData] = (__bridge id)kCFBooleanTrue;
    
    SecItemDelete((__bridge CFDictionaryRef)search);
}


@end
