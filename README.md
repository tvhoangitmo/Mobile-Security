# MobiSec Challenges - Solution Reports

This repository contains solution reports for various MobiSec challenges, covering Android application development and reverse engineering topics.

## Table of Contents

### Android Development Challenges

1. **[helloworld](appdev/helloword/helloword.md)** - Basic Android logging challenge
2. **[justlisten](appdev/justlisten/justlisten.md)** - BroadcastReceiver for receiving broadcast Intents
3. **[reachingout](appdev/reachingout/reachingout.md)** - HTTP communication and HTML parsing
4. **[justask](appdev/justask/justask.md)** - Activity result handling and Bundle parsing
5. **[filehasher](appdev/filehasher/filehasher.md)** - SHA-256 hash calculation service using Intents
6. **[whereareyou](appdev/whereareyou/whereareyou.md)** - Location service and LocationManager
7. **[jokeprovider](appdev/jokeprovider/jokeprovider.md)** - Content Provider querying
8. **[unbindable](appdev/unbindable/unbindable.md)** - Service binding and Messenger IPC
9. **[serialintent](appdev/serialintent/serialintent.md)** - Java reflection and serialization

### Reversing Challenges

1. **[babyrev](reversing/babyrev/babyrev.md)** - Basic flag format and ROT13 checks
2. **[pincode](reversing/pincode/pincode.md)** - Iterative MD5 PIN validation with remote flag fetch
3. **[gnirts](reversing/gnirts/gnirts.md)** - Split-flag hashing and reflection-based checks
4. **[goingnative](reversing/goingnative/goingnative.md)** - JNI-based flag validation combining Java and native code
5. **[blockchain](reversing/blockchain/blockchain.md)** - AES/MD5 "blockchain" style encryption and brute force
6. **[loadme](reversing/loadme/loadme.md)** - Dynamic code loading with multiple staged loaders
7. **[upos](reversing/upos/upos.md)** - Obfuscated validator with smali patching and matrix checks

### Exploitation Challenges

1. **[frontdoor](exploitation/frontdoor/frontdoor.md)** - HTTP endpoint abuse with hardcoded credentials
2. **[nojumpstarts](exploitation/nojumpstarts/nojumpstarts.md)** - Component hijacking with leaked RSA private key
3. **[fortnite](exploitation/fortnite/fortnite.md)** - External DEX replacement and payload execution
4. **[keyboard](exploitation/TB/keyboard/keyboard.md)** - Zip Slip update mechanism to overwrite prefs
5. **[filebrowser](exploitation/TB/filebrowser/filebrowser.md)** - PendingIntent command injection and DB exfiltration

---


## Flags

| Challenge | Flag |
|-----------|------|
| helloworld | `MOBISEC{here_there_is_your_first_and_last_charity_point}` |
| justlisten | `MOBISEC{not_sure_Ive_heard_well_what_did_ya_say?!?}` |
| reachingout | `MOBISEC{I_was_told_by_liars_that_http_queries_were_easy}` |
| justask | `MOBISEC{Ive_asked_and_I_got_the_flag_how_nice!}` |
| filehasher | `MOBISEC{Was_it_known_that_these_one_way_functions_give_you_back_flags?}` |
| whereareyou | `MOBISEC{Where_are_you_bro?_Will_not_tell_anybody_I_swear}` |
| jokeprovider | `MOBISEC{lol_roftl_ahahah_:D_REYAMMER_TELLS_THE_BEST_JOKES!}` |
| unbindable | `MOBISEC{please_respect_my_will_you_shall_not_bind_me_my_friend}` |
| serialintent | `MOBISEC{HOW_DID_YOU_DO_IT_THAT_WAS_SERIALLY_PRIVATE_STUFF1!!1!eleven!}` |
| babyrev | `MOBISEC{ThIs_iS_A_ReAlLy_bAsIc_rEv}` |
| pincode | `MOBISEC{local_checks_can_be_very_bad_for_security}` |
| gnirts | `MOBISEC{peppa-9876543-BAAAM-A1z9-3133337}` |
| goingnative | `MOBISEC{native_is_so-031337}` |
| blockchain | `MOBISEC{blockchain_failed_to_deliver_once_again}` |
| loadme | `MOBISEC{dynamic_code_loading_can_make_everything_tricky_eh?}` |
| upos | `MOBISEC{Isnt_this_a_truly_evil_undebuggable_piece_of_sh^W_software??}` |
| frontdoor | `MOBISEC{noob_hackers_only_check_for_backdoors}` |
| nojumpstarts | `MOBISEC{you_shall_not_jump_ok?pretty_please?}` |
| fortnite | `MOBISEC{players_gonna_play_mobisec_hackers_gonna_mobisec_it_up}` |
| keyboard | `MOBISEC{the_more_emoji_a_keyboard_has_the_more_secure_it_is}` |
| filebrowser | `MOBISEC{a_good_file_browser_would_gimme_the_flag_with_no_hacks}` |
