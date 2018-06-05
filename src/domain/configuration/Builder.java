package domain.configuration;

import domain.configuration.actions.*;
import domain.filtercontroller.FilterContainer;
import domain.filtercontroller.FilterController;
import domain.filters.Filter;

import java.util.ArrayList;
import java.util.List;

import static domain.configuration.ExistingContainerActionType.*;

/**
 * Created by Jimfi on 5/28/2018.
 */
public class Builder implements IActionObserver {

    private FilterController controller;
    private IAction newContainersAction;
    private IAction missingContainersAction;
    private IAction existingContainerAction;
    private List<IBuilderObserver> _observers;

    public Builder(FilterController controller, Configuration configuration) {
        this.controller = controller;
        this._observers = new ArrayList<>();

        if(configuration.getNewContainerActionType() == NewContainerActionType.Nothing)
            this.newContainersAction = new NullNewContainerAction();
        else if(configuration.getNewContainerActionType() == NewContainerActionType.AddFilters)
            this.newContainersAction = new AddNewFiltersNewContainerAction(this.controller, this);

        if(configuration.getMissingContainerActionType() == MissingContainerActionType.Nothing)
            this.missingContainersAction = new NullMissingContainerAction();
        else if(configuration.getMissingContainerActionType() == MissingContainerActionType.RemoveFilters)
            this.missingContainersAction = new RemoveFiltersMissingContainerAction(this.controller, this);

        if(configuration.getExistingContainerActionType() == Nothing)
            this.existingContainerAction = new NullExistingContainerAction();
        else {

            boolean add = false;
            boolean remove = false;
            boolean update = false;
            ExistingContainerActionType existingActionType = configuration.getExistingContainerActionType();
            switch (existingActionType){
                case Add:
                    add = true;
                    break;
                case AddAndRemove:
                    add = true;
                    remove = true;
                    break;
                case Remove:
                    remove = true;
                    break;
                case RemoveAndUpdate:
                    remove = true;
                    update = true;
                    break;
                case AddAndUpdate:
                    add = true;
                    update = true;
                    break;
                case AddRemoveAndUpdate:
                    add=true;
                    remove = true;
                    update = true;
                    break;
            }
            this.existingContainerAction = new ConfigurableExistingContainerAction(this.controller, this, add, remove, update);
        }


    }

    public void AddObserver(IBuilderObserver observer){
        if(this._observers.contains(observer))
            return;
        this._observers.add(observer);
    }

    public void RemoveObserver(IBuilderObserver observer){
        if(this._observers.contains(observer))
            this._observers.remove(observer);
    }

    public void ClearObservers(){
        this._observers.clear();
    }

    public void Build(BuilderItems items){
        List<FilterContainer> containers = items.GetContainers();

        if(containers == null)
            return;

        this.newContainersAction.Execute(containers);
        //this.initializeActionAndExecute(this.newContainersAction, containers);

        this.missingContainersAction.Execute(containers);
       // this.initializeActionAndExecute(this.missingContainersAction, containers);

        this.existingContainerAction.Execute(containers);
        //this.initializeActionAndExecute(this.existingContainerAction, containers);

    }

  //  private void initializeActionAndExecute(IAction action, List<FilterContainer> containers){
        //action.SetContainers(containers);
       // action.Execute();
    //}

    @Override
    public void ContainerAdded(ActionType actionType, FilterContainer container) {
        for(IBuilderObserver bo : this._observers)
            bo.ContainerAdded(actionType, container);
    }

    @Override
    public void FilterAdded(ActionType actionType, Filter f) {
        for(IBuilderObserver bo : this._observers)
            bo.FilterAdded(actionType, f);
    }

    @Override
    public void ContainerRemoved(ActionType actionType, FilterContainer container) {
        for(IBuilderObserver bo : this._observers)
            bo.ContainerRemoved(actionType, container);
    }

    @Override
    public void FilterRemoved(ActionType actionType, Filter f) {
        for(IBuilderObserver bo : this._observers)
            bo.FilterRemoved(actionType, f);
    }
}
