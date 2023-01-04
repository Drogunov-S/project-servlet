package com.tictactoe.controllers;

import com.tictactoe.Field;
import com.tictactoe.Sign;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession currentSession = req.getSession();
        //Получаем поле из сессии
        Field field = extractField(currentSession);
        //Индекс ячейки на котурую нажали на фронте
        int index = getSelectedIndex(req);
        //Получаем значение ячейки на поле
        Sign currentSign = field.getField().get(index);
        //Проверяем что ячейка пустая
        if (Sign.EMPTY != currentSign) {
            RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            requestDispatcher.forward(req, resp);
            return;
        }
        // ставим крестик в ячейке, по которой кликнул пользователь
        field.getField().put(index, Sign.CROSS);
        if (checkWin(resp, currentSession, field)) {
            return;
        }
        //Пустая ячейка
        int emptyFieldIndex = field.getEmptyFieldIndex();
        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(resp, currentSession, field)) {
                return;
            }
        } else {
            currentSession.setAttribute("draw", true);
            currentSession.setAttribute("data", field.getFieldData());
            resp.sendRedirect("/index.jsp");
            return;
        }
        // Значения полей для фронта
        List<Sign> data = field.getFieldData();
        // Обновляем объект поля и список значков в сессии
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);
        //Перенапрвляем на другой сервлет
        resp.sendRedirect("/index.jsp");
    }
    
    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }
    
    private int getSelectedIndex(HttpServletRequest req) {
        String value = req.getParameter("click");
        boolean isDigits = value.chars().allMatch(Character::isDigit);
        return isDigits ? Integer.parseInt(value) : 0;
    }
    
    private boolean checkWin(HttpServletResponse resp, HttpSession session, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            session.setAttribute("winner", winner);
            List<Sign> data = field.getFieldData();
            session.setAttribute("data", data);
            resp.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }
}
