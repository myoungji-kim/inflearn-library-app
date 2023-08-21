package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor (
    private val userRepository: UserRepository,
    private val userService: UserService,
) {
    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("유저 정보 저장이 정상적으로 진행")
    fun saveUserTest() {
        // given
        val request = UserCreateRequest("김명지", null)

        // when
        userService.saveUser(request)

        // then
        val results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("김명지")
        assertThat(results[0].age).isNull()
    }

    @Test
    @DisplayName("유저 정보 조회가 정상적으로 진행")
    fun getUsersTest() {
        // given
        userRepository.saveAll(listOf(
            User("A", 20),
            User("B", null),
        ))

        // when
        val results = userService.getUsers()

        // then
        /**
         * 두 테스트는 Spring Context를 공유하기 때문에 클래스 전체 테스트를 실행하면
         * 최초 생성 테스트인 saveUserTest() 의 영향으로 사용자 목록을 조회한 results의 size가 3이 되어 test failed
         * => 테스트가 끝날 때마다 DB 내 데이터를 초기화 해주는 작업이 별도로 필요 (@AfterEach)
         */
        assertThat(results).hasSize(2) // [UserResponse(), UserResponse()]
        assertThat(results).extracting("name").containsExactlyInAnyOrder("A", "B") // ["A", "B"]
        assertThat(results).extracting("age").containsExactlyInAnyOrder(20, null)
    }

    /**
     * 저장한 A 유저의 정보를 불러와 ID를 변경해준 뒤, 잘 변경되었는지 확인
     */
    @Test
    @DisplayName("유저 업데이트가 정상적으로 진행")
    fun updateUserNameTest() {
        // given
        val savedUser = userRepository.save(User("A", null))
        val request = UserUpdateRequest(savedUser.id, "B")

        // when
        userService.updateUserName(request)

        // then
        val result = userRepository.findAll()[0]
        assertThat(result.name).isEqualTo("B")
    }

    /**
     * DB 내 저장된 사용자 정보를 삭제한 후,
     * DB에 데이터가 비어있는지 확인
     */
    @Test
    @DisplayName("유저 삭제가 정상적으로 진행")
    fun deleteUserTest() {
        // given
        userRepository.save(User("A", null))

        // when
        userService.deleteUser("A")

        // then
        assertThat(userRepository.findAll()).isEmpty()
    }
}