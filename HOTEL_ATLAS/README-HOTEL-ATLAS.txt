HOTEL ATLAS - VERSAO COM LOGIN E RESERVAS

Como abrir:
1. Abra a pasta HOTEL_ATLAS no IntelliJ IDEA.
2. Aguarde o Maven carregar as dependencias.
3. Execute com Maven: javafx:run (ou execute HelloApplication).

Acessos demonstrativos:
- Cliente: cliente@atlas.com | senha: 123456
- Funcionário: funcionario@atlas.com | senha: 123456

O que foi implementado:
- Tela inicial de login com seleção de perfil.
- Área do funcionário com dashboard, calendário, quartos e reservas.
- Área do cliente com datas de check-in/check-out, quantidade de hóspedes,
  comparação de quartos por categoria, capacidade, qualidade e diária.
- Cálculo do total por número de diárias.
- Verificação de conflito de datas para impedir duas reservas no mesmo quarto.
- Confirmação de reserva e listagem na área administrativa.

IMPORTANTE:
Esta versão é funcional para demonstração, mas os dados ficam somente em memória.
Ao fechar o programa, as reservas criadas são perdidas. Os usuários e senhas são
credenciais de demonstração fixas no código. Para uso real, será necessário
integrar banco de dados, cadastro de clientes e autenticação segura (senhas com hash).
