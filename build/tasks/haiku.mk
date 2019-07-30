my_modules := \
    libexif_parser_fuzzer \
    libopus_fuzzer \
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

my_package_name := haiku

include $(BUILD_SYSTEM)/tasks/tools/package-modules.mk

.PHONY: haiku
haiku : $(my_package_zip)

name := $(TARGET_PRODUCT)-haiku-$(FILE_NAME_TAG)
$(call dist-for-goals, haiku, $(my_package_zip):$(name).zip)

