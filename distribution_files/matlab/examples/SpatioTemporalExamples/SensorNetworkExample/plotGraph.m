function plotGraph(spatialModel, numFrame, s)
figure1 = figure('Name','Spatial Model')
Gvor = spatialModel{numFrame};
A = adjacency(Gvor);
G = graph(A);
p = plot(G,'r','XData',Gvor.Nodes.x,'YData',Gvor.Nodes.y);
p.EdgeColor = 'black';
p.MarkerSize = 25;
p.NodeFontSize = 20;
p.EdgeFontSize = 25;
set(gca,'FontSize',18);    
%colorbar('FontSize',20);
if s == 'node'
    %colormap hsv
    p.NodeCData = cell2mat(Gvor.Nodes.nodeType);
elseif s == 'batt'
    p.NodeCData = Gvor.Nodes.battery;
elseif s == 'temp'
    p.NodeCData = Gvor.Nodes.temperature;
colorbar
end
end

