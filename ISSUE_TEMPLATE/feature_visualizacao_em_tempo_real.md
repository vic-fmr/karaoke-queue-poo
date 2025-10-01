# [Feature] Visualização em tempo real da fila de músicas com identificação de participantes

## Descrição:
Como um Participante, eu quero ver a lista de músicas na fila em tempo real, com o nome de quem as adicionou, para que eu saiba qual é a próxima música e quando será a minha vez de cantar.

## Componentes (Figma):
- Uma lista ordenada que mostra "Nome da Música - Nome do Participante".
- A música atual pode ter um destaque visual (o destaque ainda não está definido, precisa ser implementado de acordo com a necessidade do time/produto).

## Critérios de aceitação - positivo:
- Dado que estou conectado a uma sessão.
- Quando novas músicas são adicionadas.
- Então devo ver a lista atualizada em tempo real.

## Critérios de aceitação - positivo:
- Dado que uma música está sendo reproduzida.
- Quando olho a fila.
- Então essa música deve estar visualmente destacada.

## Notas técnicas:
- Todos os participantes podem adicionar músicas e visualizar a lista geral.
- A atualização em tempo real deve ser feita via WebSocket.
- O destaque visual para a música atual ainda não foi definido, apenas sinalizar a necessidade de implementação.
- Foco apenas na implementação visual/funcional, não é necessário implementar testes automatizados neste momento.
- O frontend deve ser implementado em Angular.