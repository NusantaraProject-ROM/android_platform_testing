my_modules := $(haiku)

my_package_name := haiku

.PHONY: haiku

#haiku : $(my_package_zip)

haiku : \
    libexif_parser_fuzzer \
    libopus_fuzzer \
    mtp_fuzzer \
    simple_test_fuzzer \
    base_json_reader_fuzzer \
    libhevc_fuzzer \
    libsonivox_fuzzer \
    regexec_fuzzer \
    tremolo_fuzzer \
    cxa_demangle_fuzzer \
    libldacBT_enc_fuzzer \
    libutils_fuzzer \
    sanitycheck_fuzzer \
    libsonivox_fuzzer \
    tremolo_fuzzer \
        
$(call $(my_package_zip):$(name).zip)
