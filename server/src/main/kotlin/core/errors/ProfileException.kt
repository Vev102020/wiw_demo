package core.errors

import com.wiwchat.shared.common.domain.errors.ProfileErrorCode
import io.ktor.http.HttpStatusCode


sealed class ProfileException(
    code: ProfileErrorCode,
    message: String,
    httpStatus: HttpStatusCode
) : AppException(code, message, httpStatus)


class EmptyDisplayNameException : ProfileException(
    code = ProfileErrorCode.PROFILE_EMPTY_DISPLAY_NAME,
    message = "Отображаемое имя не может быть пустым.",
    httpStatus = HttpStatusCode.BadRequest,
)

class InvalidDisplayNameLengthException : ProfileException(
    code = ProfileErrorCode.PROFILE_DISPLAY_NAME_INVALID_LENGTH,
    message = "Некорректная длина отображаемого имени.",
    httpStatus = HttpStatusCode.BadRequest,
)

// Ошибки для username
class EmptyUsernameException : ProfileException(
    code = ProfileErrorCode.PROFILE_EMPTY_USERNAME,
    message = "Имя пользователя (username) не может быть пустым.",
    httpStatus = HttpStatusCode.BadRequest,
)

class InvalidUsernameLengthException : ProfileException(
    code = ProfileErrorCode.PROFILE_USERNAME_INVALID_LENGTH,
    message = "Некорректная длина имени пользователя.",
    httpStatus = HttpStatusCode.BadRequest,
)

class InvalidUsernameFormatException : ProfileException(
    code = ProfileErrorCode.PROFILE_USERNAME_INVALID_FORMAT,
    message = "Имя пользователя может содержать только латинские буквы, цифры и нижнее подчеркивание.",
    httpStatus = HttpStatusCode.BadRequest,
)

class UsernameAlreadyTakenException : ProfileException(
    code = ProfileErrorCode.PROFILE_USERNAME_ALREADY_TAKEN,
    message = "Это имя пользователя уже занято.",
    httpStatus = HttpStatusCode.Conflict,
)