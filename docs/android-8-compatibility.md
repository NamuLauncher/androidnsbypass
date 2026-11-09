# Android 8 namespace 호환

API 26에서 라이브러리 초기화가 `__loader_android_link_namespaces_all_libs`를 찾지 못해 `exit(121)`로 프로세스를 종료했다. 게임 실행 전 Application 초기화에서도 네이티브 의존성이 로드되므로 계측이 시작되지 않았다.

Android 8.0(API 26)과 8.1(API 27)의 linker는 namespace 생성과 SONAME을 지정한 연결을 제공하지만 all-libs 연결은 제공하지 않는다. 이 기능은 Android 9(API 28)에 있다. [AOSP 8.0 export](https://android.googlesource.com/platform/bionic/+/android-8.0.0_r1/linker/dlfcn.cpp#203), [AOSP 8.1 export](https://android.googlesource.com/platform/bionic/+/android-8.1.0_r1/linker/dlfcn.cpp#203), [AOSP 9 구현](https://android.googlesource.com/platform/bionic/+/android-9.0.0_r1/linker/dlfcn.cpp#271).

API 26–27에서만 all-libs 함수 조회·필수 검사를 생략한다. 기본 namespace 함수는 계속 필수이고, API 28 이상에서는 all-libs 함수도 계속 필수다. 지원하지 않는 플랫폼에서 C·JNI·compat all-libs 호출은 false를 반환한다. 빈 SONAME 목록이나 wildcard를 이용해 전체 연결 성공을 흉내 내지 않는다. 기존 namespace 생성·이름 지정 연결·동적 로딩은 유지한다. Android 7 이하의 지원 범위는 이번 수정에서 바꾸지 않는다.

`ENABLE_TESTS` 자체 진단도 지원하지 않는 all-libs 포인터를 호출하지 않으며 이름 지정 연결을 검증한다. 필수 namespace 포인터 검사 이후에 진단을 실행한다.

`NamespaceCompatibilityTest`의 두 계측은 실제 JNI 초기화, namespace 생성·이름 지정 연결, libc의 dlopen·dlsym·dlclose와 API별 all-libs 반환값을 확인한다. API 26과 API 28 이상에서 같은 테스트를 실행해 양쪽 경로를 확인한다. namespace를 제거하는 공개 linker API가 없어 테스트가 만든 namespace는 테스트 프로세스 수명 동안 유지된다.

이 문서는 수정 계약과 검증 대상을 설명한다. 실제 기기별 실행 결과는 해당 PR에 기록한다. 라이브러리 계측 통과와 호스트 앱·게임의 실행 성공은 구분한다.
